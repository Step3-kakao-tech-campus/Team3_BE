package com.bungaebowling.server.user.controller;


import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

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
}
