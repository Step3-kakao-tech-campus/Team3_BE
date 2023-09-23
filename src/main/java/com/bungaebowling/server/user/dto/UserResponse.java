package com.bungaebowling.server.user.dto;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;

import java.util.List;

public class UserResponse {

    public record GetUsersDto(
            CursorRequest nextCursorRequest,
            List<UserDto> users
    ) {
        public record UserDto(
                Long id,
                String name,
                Double rating,
                String profileImage
        ) {

        }
    }
}
