package com.bungaebowling.server.user.controller;


import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import com.bungaebowling.server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    final private UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinDto requestDto, Errors errors) throws URISyntaxException {

        var response = ApiUtils.success(HttpStatus.CREATED);
        return ResponseEntity.created(new URI("/api/users/3")).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest.LoginDto requestDto, Errors errors) {
        var tokens = userService.login(requestDto);

        var responseCookie = createRefreshTokenCookie(tokens.refresh());

        var response = ApiUtils.success();
        return ResponseEntity.ok().header(JwtProvider.HEADER, tokens.access())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.logout(userDetails.getId());

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .build();

        var response = ApiUtils.success();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/authentication")
    public ResponseEntity<?> reIssueTokens(@CookieValue("refreshToken") String refreshToken) {

        var tokens = userService.reIssueTokens(refreshToken);

        var responseCookie = createRefreshTokenCookie(tokens.refresh());

        var response = ApiUtils.success();
        return ResponseEntity.ok().header(JwtProvider.HEADER, tokens.access())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        CursorRequest cursorRequest = new CursorRequest(1L, 20);
        List<UserResponse.GetUsersDto.UserDto> userDtos = new ArrayList<>();
        var userDto1 = new UserResponse.GetUsersDto.UserDto(
                1L,
                "김볼링12",
                4.8,
                null
        );
        userDtos.add(userDto1);

        var userDto2 = new UserResponse.GetUsersDto.UserDto(
                2L,
                "12김볼링",
                4.8,
                null
        );
        userDtos.add(userDto2);

        var userDto3 = new UserResponse.GetUsersDto.UserDto(
                3L,
                "12김볼링24",
                4.8,
                null
        );
        userDtos.add(userDto3);

        var getUsersDto = new UserResponse.GetUsersDto(cursorRequest, userDtos);

        var response = ApiUtils.success(getUsersDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        var getUserDto = new UserResponse.GetUserDto(
                "김볼링",
                190,
                4.8,
                "부산광역시 진구 부전동",
                null
        );

        var response = ApiUtils.success(getUserDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/users/{userId}/records")
    public ResponseEntity<?> getUserRecords(@PathVariable Long userId) {
        var getRecordDto = new UserResponse.GetRecordDto(
                20,
                160,
                180,
                110
        );

        var response = ApiUtils.success(getRecordDto);
        return ResponseEntity.ok().body(response);
    }

    private static ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // javascript 접근 방지
                .secure(true) // https 통신 강제
                .maxAge(JwtProvider.REFRESH_EXP_SECOND)
                .build();
    }
}
