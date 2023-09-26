package com.bungaebowling.server.user.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class UserService {

    final private UserRepository userRepository;

    final private RedisTemplate<String, String> redisTemplate;

    final private PasswordEncoder passwordEncoder;

    public UserResponse.TokensDto login(UserRequest.loginDto requestDto) {
        var user = userRepository.findByEmail(requestDto.email()).orElseThrow(() ->
                new Exception400("이메일 혹은 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(requestDto.password(), user.getPassword())) {
            throw new Exception400("이메일 혹은 비밀번호가 일치하지 않습니다.");
        }

        var access = JwtProvider.createAccess(user);
        var refresh = JwtProvider.createRefresh(user);

        redisTemplate.opsForValue().set(
                user.getId().toString(),
                refresh,
                JwtProvider.REFRESH_EXP_SECOND,
                TimeUnit.SECONDS
        );

        return new UserResponse.TokensDto(access, refresh);
    }

    public void logout(Long id) {
        redisTemplate.delete(id.toString());
    }
}
