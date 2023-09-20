package com.bungaebowling.server._core.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bungaebowling.server.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class JwtProvider {
    public static final Long ACCESS_EXP_SECOND = 60L * 60 * 24 * 2; // 토큰 유효기간 2일 <- 개발용
    public static final Long REFRESH_EXP_SECOND = 60L * 60 * 24 * 30; // 30일
    public static final String TOKEN_PREFIX = "Bearer "; // 스페이스 필요함
    public static final String HEADER = "Authorization";
    private static String SECRET;

    @Value("${bungaebowling.secret}")
    public void setKey(String value) {
        SECRET = value;
    }

    public static String createAccess(User user) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expired = now.plusSeconds(ACCESS_EXP_SECOND);
        String jwt = JWT.create()
                .withExpiresAt(Timestamp.valueOf(expired))
                .withClaim("id", user.getId())
                .withClaim("role", String.valueOf(user.getRole()))
                .sign(Algorithm.HMAC512(SECRET));
        return TOKEN_PREFIX + jwt;
    }

    public static String createRefresh(User user) {
        // TODO: 리프레시 토큰에 무슨 데이터를 넣어야할 지 몰라서 임시로 id와 유효기간만 넣어놨습니다.
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expired = now.plusSeconds(REFRESH_EXP_SECOND);
        String jwt = JWT.create()
                .withExpiresAt(Timestamp.valueOf(expired))
                .withClaim("id", user.getId())
                .sign(Algorithm.HMAC512(SECRET));
        return TOKEN_PREFIX + jwt;
    }

    public static DecodedJWT verify(String jwt) {
        return JWT.require(Algorithm.HMAC512(SECRET)).build()
                .verify(jwt.replace(TOKEN_PREFIX, ""));
    }
}
