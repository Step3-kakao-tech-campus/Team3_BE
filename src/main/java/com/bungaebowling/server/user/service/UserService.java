package com.bungaebowling.server.user.service;

import com.auth0.jwt.interfaces.DecodedJWT;
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
import com.bungaebowling.server.score.service.ScoreService;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import com.bungaebowling.server.user.rate.UserRate;
import com.bungaebowling.server.user.rate.repository.UserRateRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
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
    //private final RestTemplate restTemplate;

    private final AwsS3Service awsS3Service;
    private final ScoreService scoreService;

    private final Environment environment;

    @Value("${bungaebowling.domain}")
    private String domain;
    @Value("${mail.server}")
    private String mailServer;
    @Value("${mail.username}")
    private String username;
    @Value("${mail.password}")
    private String password;

    @Transactional
    public UserResponse.JoinDto join(UserRequest.JoinDto requestDto) {
        long districtId;
        try {
            districtId = Long.parseLong(requestDto.districtId());
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_DATA, "숫자만 가능합니다.:districtId");
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

        if (!Objects.equals(redisTemplate.opsForValue().get(decodedRefreshToken.getSubject()), refreshToken))
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 refresh 토큰입니다.");

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

        if (Arrays.asList(environment.getActiveProfiles()).contains("deploy")) {
            sendMailToMailServer(user, subject, text);
        } else {
            sendMail(user, subject, text);
        }
    }

    private void sendMailToMailServer(User user, String subject, String text) {
        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("krmp-proxy.9rum.cc", 3128));
            requestFactory.setProxy(proxy);
            RestTemplate restTemplate = new RestTemplate(requestFactory);


            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            MultiValueMap<String, String> requests = new LinkedMultiValueMap<>();
            requests.add("subject", subject);
            requests.add("text", text);
            requests.add("email", user.getEmail());
            requests.add("username", username);
            requests.add("password", password);

            log.info("json: "+ requests);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requests, httpHeaders);
            String requestURL = "https://" + mailServer + ":5000/email";

            log.info("requestURL: "+ requestURL);

            restTemplate.postForEntity(requestURL, request, String.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_LIMIT_EXCEEDED);
        }
    }

    private void sendMail(User user, String subject, String text) {
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
        String searchName = name != null ? name : "";
        List<User> users = loadUsers(cursorRequest, searchName);
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
        int average = scoreService.calculateAverage(scores);
        return new UserResponse.GetUserDto(user, rating, average);
    }

    public UserResponse.GetMyProfileDto getMyProfile(Long userId) {
        User user = findUserById(userId);
        double rating = getRating(userId);
        List<Score> scores = findScoreByUserId(userId);
        int average = scoreService.calculateAverage(scores);
        return new UserResponse.GetMyProfileDto(user, rating, average);
    }

    @Transactional
    public void updateMyProfile(MultipartFile profileImage, String name, Long districtId, Long userId) {
        validCheckName(name);

        User user = findUserById(userId);

        if (userRepository.existsByName(name)) {
            throw new CustomException(ErrorCode.USER_NAME_DUPLICATED);
        }

        District district = districtId == null ? null :
                districtRepository.findById(districtId).orElseThrow(
                        () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
                );

        try {
            if (profileImage == null) {
                user.updateProfile(name, district, null, null);
            } else {
                updateProfileWithImage(user, name, district, profileImage);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.USER_UPDATE_FAILED);
        }
    }

    private void validCheckName(String name) {
        if (name != null) {
            if (name.length() > 20) {
                throw new CustomException(ErrorCode.INVALID_REQUEST_DATA, "최대 20자까지 입니다." + ":name");
            }
            if (!Pattern.matches("[a-zA-Z0-9가-힣]*", name)) {
                throw new CustomException(ErrorCode.INVALID_REQUEST_DATA, "한글, 영문, 숫자만 입력 가능합니다.");
            }
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

    @Transactional
    public void updatePassword(Long userId, UserRequest.UpdatePasswordDto requestDto) {
        User user = findUserById(userId);
        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.WRONG_PASSWORD);
        }
        user.updatePassword(passwordEncoder.encode(requestDto.newPassword()));
    }


    public void sendVerificationMailForPasswordReset(UserRequest.SendVerificationMailForPasswordResetDto requestDto) {
        User user = userRepository.findByEmail(requestDto.email()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String token = JwtProvider.createEmailVerificationForPassword(user);

        String subject = "[번개볼링] 비밀번호 초기화 및 임시 비밀번호 발급을 위한 이메일 인증을 완료해주세요.";
        String text = "<a href='" + domain + "/password/email-verification?token=" + token + "'>링크</a>를 클릭하여 인증을 완료해주세요!";

        if (Arrays.asList(environment.getActiveProfiles()).contains("deploy")) {
            sendMailToMailServer(user, subject, text);
        } else {
            sendMail(user, subject, text);
        }
    }

    @Transactional
    public void confirmEmailAndSendTempPassword(UserRequest.ConfirmEmailAndSendTempPasswordDto requestDto) {
        DecodedJWT decodedJwt = JwtProvider.verify(requestDto.token(), JwtProvider.TYPE_EMAIL_VERIFICATION_PASSWORD);
        User user = findUserById(Long.valueOf(decodedJwt.getSubject()));
        String tempPassword = getRamdomPassword(15);
        user.updatePassword(passwordEncoder.encode(tempPassword));

        String subject = "[번개볼링] 임시 비밀번호";
        String text = "임시 비밀번호는  " + tempPassword + "  입니다. <br>*비밀번호를 변경해주세요." + "<br>*기존의 비밀번호는 사용할 수 없습니다.";

        if (Arrays.asList(environment.getActiveProfiles()).contains("deploy")) {
            sendMailToMailServer(user, subject, text);
        } else {
            sendMail(user, subject, text);
        }
    }

    public UserResponse.GetRecordDto getRecords(Long userId) {
        User user = findUserById(userId);
        List<Score> scores = findScoreByUserId(userId);
        int game = countGames(user);
        int average = scoreService.calculateAverage(scores);
        int maximum = scoreService.findMaxScore(scores);
        int minimum = scoreService.findMinScore(scores);
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

    private double getRating(Long userId) {
        return userRateRepository.findAllByUserId(userId).stream()
                .mapToInt(UserRate::getStarCount)
                .average().orElse(0.0);
    }

    private Long getLastKey(List<User> users) {
        return users.isEmpty() ? CursorRequest.NONE_KEY : users.get(users.size() - 1).getId();
    }

    public String getRamdomPassword(int length) {
        char[] charSet = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '!', '@', '#', '$', '%', '^', '&'};

        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();

        int charSetLength = charSet.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(charSet[random.nextInt(charSetLength)]);
        }

        return stringBuilder.toString();

    }
}