package com.bungaebowling.server.user.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server._core.utils.AwsS3Service;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.applicant.repository.ApplicantRepository;
import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.score.repository.ScoreRepository;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import com.bungaebowling.server.user.rate.UserRate;
import com.bungaebowling.server.user.rate.repository.UserRateRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    public static final int DEFAULT_SIZE = 20;

    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final UserRateRepository userRateRepository;
    private final ScoreRepository scoreRepository;
    private final ApplicantRepository applicantRepository;

    private final RedisTemplate<String, String> redisTemplate;

    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender javaMailSender;

    private final AwsS3Service awsS3Service;

    @Value("${bungaebowling.domain}")
    private String domain;

    @Transactional
    public UserResponse.JoinDto join(UserRequest.JoinDto requestDto) {
        long districtId;
        try {
            districtId = Long.parseLong(requestDto.districtId());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_RUQUEST_DATA, "숫자만 가능합니다.:districtId");
        }

        if (userRepository.findByEmail(requestDto.email()).isPresent())
            throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATED);

        if (userRepository.findByName(requestDto.name()).isPresent())
            throw new CustomException(ErrorCode.USER_NAME_DUPLICATED);

        var district = districtRepository.findById(districtId).orElseThrow(() ->
                new CustomException(ErrorCode.REGION_NOT_FOUND));

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
                new CustomException(ErrorCode.LOGIN_FAILED, "이메일 혹은 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED, "이메일 혹은 비밀번호가 일치하지 않습니다.");
        }

        return issueTokens(user);
    }

    public void logout(Long id) {
        redisTemplate.delete(id.toString());
    }

    public UserResponse.TokensDto reIssueTokens(String refreshToken) {
        var decodedRefreshToken = JwtProvider.verify(refreshToken, JwtProvider.TYPE_REFRESH);

        var user = userRepository.findById(Long.valueOf(decodedRefreshToken.getSubject())).orElseThrow(() ->
                new CustomException(ErrorCode.UNKNOWN_SERVER_ERROR, "재발급 과정에서 오류가 발생했습니다."));

        return issueTokens(user);
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

    public void sendVerificationMail(Long userId) {

        var user = findUserById(userId);

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
            throw new CustomException(ErrorCode.EMAIL_SEND_LIMIT_EXCEEDED);
        }
    }

    @Transactional
    public void confirmEmail(UserRequest.ConfirmEmailDto requestDto) {
        var decodedJwt = JwtProvider.verify(requestDto.token(), JwtProvider.TYPE_EMAIL_VERIFICATION);

        var user = findUserById(Long.valueOf(decodedJwt.getSubject()));

        user.updateRole(Role.ROLE_USER);
    }

    public UserResponse.GetUsersDto getUsers(CursorRequest cursorRequest, String name) {
        List<User> users = loadUsers(cursorRequest, name);
        List<Double> ratings = users.stream()
                .map(user -> getRating(user.getId()))
                .toList();
        Long lastKey = getLastKey(users);
        return UserResponse.GetUsersDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), users, ratings);
    }

    private List<User> loadUsers(CursorRequest cursorRequest, String name) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);
        if (!cursorRequest.hasKey()) {
            return userRepository.findAllByNameContainingOrderByIdDesc(name, pageable);
        } else {
            return userRepository.findAllByNameContainingAndIdLessThanOrderByIdDesc(name, cursorRequest.key(), pageable);
        }
    }

    public UserResponse.GetUserDto getUser(Long userId) {
        User user = findUserById(userId);
        double rating = getRating(userId);
        List<Score> scores = findScoreByUserId(userId);
        int average = calculateAverage(scores);
        return new UserResponse.GetUserDto(user, rating, average);
    }

    public UserResponse.GetMyProfileDto getMyProfile(Long userId) {
        User user = findUserById(userId);
        double rating = getRating(userId);
        List<Score> scores = findScoreByUserId(userId);
        int average = calculateAverage(scores);
        return new UserResponse.GetMyProfileDto(user, rating, average);
    }

    @Transactional
    public void updateMyProfile(MultipartFile profileImage, UserRequest.UpdateMyProfileDto request, Long userId) {
        User user = findUserById(userId);

        District district = request.districtId() == null ? null :
                districtRepository.findById(request.districtId()).orElseThrow(
                        () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
                );

        if (profileImage == null) {
            user.updateProfile(request.name(), district, null, null);
        } else {
            updateProfileWithImage(user, request.name(), district, profileImage);
        }
    }

    private void updateProfileWithImage(User user, String name, District district, MultipartFile profileImage) {
        if (user.getImgUrl() != null) {
            awsS3Service.deleteFile(user.getResultImageUrl());
        }

        LocalDateTime updateTime = LocalDateTime.now();
        String resultImageUrl = awsS3Service.uploadProfileFile(user.getId(), "profile", updateTime, profileImage);
        String accessImageUrl = awsS3Service.getImageAccessUrl(resultImageUrl);

        user.updateProfile(name, district, resultImageUrl, accessImageUrl);
    }

    public UserResponse.GetRecordDto getRecords(Long userId) {
        User user = findUserById(userId);
        List<Score> scores = findScoreByUserId(userId);
        int game = countGames(user);
        int average = calculateAverage(scores);
        int maximum = findMaxScore(scores);
        int minimum = findMinScore(scores);
        return new UserResponse.GetRecordDto(user.getName(), game, average, maximum, minimum);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private List<Score> findScoreByUserId(Long userId) {
        return scoreRepository.findAllByUserId(userId);
    }

    private int countGames(User user) {
        return applicantRepository.findAllByUserIdAndPostIsCloseTrueAndStatusTrue(user.getId()).size();
    }

    private int calculateAverage(List<Score> scores) {
        return (int) scores.stream()
                .mapToInt(Score::getScoreNum)
                .average()
                .orElse(0.0);
    }

    private int findMaxScore(List<Score> scores) {
        return scores.stream()
                .mapToInt(Score::getScoreNum)
                .max()
                .orElse(0);
    }

    private int findMinScore(List<Score> scores) {
        return scores.stream()
                .mapToInt(Score::getScoreNum)
                .min()
                .orElse(0);
    }

    private double getRating(Long userId) {
        return userRateRepository.findAllByUserId(userId).stream()
                .mapToInt(UserRate::getStarCount)
                .average().orElse(0.0);
    }

    private Long getLastKey(List<User> users) {
        return users.isEmpty() ? CursorRequest.NONE_KEY : users.get(users.size() - 1).getId();
    }
}