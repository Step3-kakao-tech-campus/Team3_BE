package com.bungaebowling.server.user.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.errors.exception.server.Exception500;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import com.bungaebowling.server.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    final private UserRepository userRepository;

    final private DistrictRepository districtRepository;

    final private RedisTemplate<String, String> redisTemplate;

    final private PasswordEncoder passwordEncoder;

    final private JavaMailSender javaMailSender;

    @Value("${bungaebowling.domain}")
    private String domain;

    @Transactional
    public UserResponse.JoinDto join(UserRequest.JoinDto requestDto) {
        long districtId;
        try {
            districtId = Long.parseLong(requestDto.districtId());
        } catch (NumberFormatException e) {
            throw new Exception400("숫자만 가능합니다.:districtId");
        }

        if (userRepository.findByEmail(requestDto.email()).isPresent())
            throw new Exception400("이미 존재하는 이메일입니다.");

        if (userRepository.findByName(requestDto.name()).isPresent())
            throw new Exception400("이미 존재하는 닉네임입니다.");

        var district = districtRepository.findById(districtId).orElseThrow(() ->
                new Exception404("존재하지 않는 행정구역 id입니다."));

        var encodedPassword = passwordEncoder.encode(requestDto.password());

        var user = requestDto.createUser(district, encodedPassword);

        var savedUser = userRepository.save(user);

        var tokens = issueTokens(savedUser);

        return new UserResponse.JoinDto(
                savedUser.getId(),
                tokens.access(),
                tokens.refresh()
        );
    }

    public UserResponse.TokensDto login(UserRequest.LoginDto requestDto) {
        var user = userRepository.findByEmail(requestDto.email()).orElseThrow(() ->
                new Exception400("이메일 혹은 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new Exception400("이메일 혹은 비밀번호가 일치하지 않습니다.");
        }

        return issueTokens(user);
    }

    public void logout(Long id) {
        redisTemplate.delete(id.toString());
    }

    public UserResponse.TokensDto reIssueTokens(String refreshToken) {
        var decodedRefreshToekn = JwtProvider.verify(refreshToken, JwtProvider.TYPE_REFRESH);

        var user = userRepository.findById(Long.valueOf(decodedRefreshToekn.getSubject())).orElseThrow(() ->
                new Exception500("재발급 과정에서 오류가 발생했습니다."));

        return issueTokens(user);
    }

    public void sendVerificationMail(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new Exception400("유저를 찾을 수 없습니다."));

        var token = JwtProvider.createEmailVerification(user);

        String subject = "[번개볼링] 이메일 인증을 완료해주세요.";
        String text = "<a href='" + domain + "/email-verification?token=" + token + "'>링크</a>를 클릭하여 인증을 완료해주세요!";

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(text, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new Exception500("서버 이메일 전송 한도가 초과되었습니다. 내일 다시 시도해주세요.");
        }
    }

    private UserResponse.TokensDto issueTokens(User user) {
        var access = JwtProvider.createAccess(user);
        var refresh = JwtProvider.createRefresh(user);

        redisTemplate.opsForValue().set(
                user.getId().toString(),
                refresh,
                JwtProvider.getRefreshExpSecond(),
                TimeUnit.SECONDS
        );

        return new UserResponse.TokensDto(access, refresh);
    }
}
