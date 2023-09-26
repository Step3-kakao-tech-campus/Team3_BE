package com.bungaebowling.server.user.controller;


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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    final private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest.loginDto requestDto, Errors errors) {
        UserResponse.TokensDto tokens = userService.login(requestDto);



        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", tokens.refresh())
                .httpOnly(true) // javascript 접근 방지
                .secure(true) // https 통신 강제
                .build();

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
}
