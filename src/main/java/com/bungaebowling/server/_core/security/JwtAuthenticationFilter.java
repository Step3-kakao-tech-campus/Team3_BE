package com.bungaebowling.server._core.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String jwt = request.getHeader(JwtProvider.HEADER);

        if (jwt == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            DecodedJWT decodedJWT = JwtProvider.verify(jwt, JwtProvider.TYPE_ACCESS);
            Long id = Long.valueOf(decodedJWT.getSubject());
            Role role = decodedJWT.getClaim("role").as(Role.class);
            User user = User.builder()
                    .id(id)
                    .role(role)
                    .build();
            CustomUserDetails userDetails = new CustomUserDetails(user);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            userDetails.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("인증 객체 생성됨");
        } catch (TokenExpiredException e) {
            log.error("토큰 만료");
        } catch (Exception e) {
            log.error("토큰 검증 실패");
        } finally {
            chain.doFilter(request, response);
        }
    }
}
