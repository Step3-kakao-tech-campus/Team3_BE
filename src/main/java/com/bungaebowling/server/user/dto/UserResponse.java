package com.bungaebowling.server.user.dto;

import com.bungaebowling.server._core.utils.CursorRequest;

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

    public record GetUserDto(
            String name,
            Integer averageScore,
            Double rating,
            String address,
            String profileImage
    ){

    }

    public record GetRecordDto(
            Integer game,
            Integer average,
            Integer maximum,
            Integer minimum
    ) {

    }

    public record JoinDto(
            Long savedId,
            String access,
            String refresh
    ) {}

    public record TokensDto(
            String access,
            String refresh
    ) {}
}
