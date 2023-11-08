package com.bungaebowling.server.user.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;

import java.util.List;
import java.util.stream.IntStream;

public class UserResponse {

    public record GetUsersDto(
            CursorRequest nextCursorRequest,
            List<UserDto> users
    ) {
        public static GetUsersDto of(CursorRequest nextCursorRequest, List<User> users,
                                     List<Double> ratings) {
            return new GetUsersDto(
                    nextCursorRequest,
                    IntStream.range(0, users.size())
                            .mapToObj(index -> new UserDto(users.get(index), ratings.get(index))).toList());
        }

        public record UserDto(
                Long id,
                String name,
                Double rating,
                String profileImage
        ) {
            public UserDto(User user, Double rating) {
                this(
                        user.getId(),
                        user.getName(),
                        rating,
                        user.getImgUrl()
                );
            }
        }
    }

    public record GetUserDto(
            String name,
            Integer averageScore,
            Double rating,
            String address,
            String profileImage
    ) {
        public GetUserDto(User user, Double rating, Integer average) {
            this(
                    user.getName(),
                    average,
                    rating,
                    user.getDistrictName(),
                    user.getImgUrl()
            );
        }
    }

    public record GetMyProfileDto(
            Long id,
            String name,
            String email,
            Boolean verification,
            Integer averageScore,
            Double rating,
            Long districtId,
            String address,
            String profileImage
    ) {
        public GetMyProfileDto(User user, Double rating, Integer average) {
            this(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole() == Role.ROLE_USER,
                    average,
                    rating,
                    user.getDistrict().getId(),
                    user.getDistrictName(),
                    user.getImgUrl()
            );
        }
    }

    public record GetRecordDto(
            String name,
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
    ) {
    }

    public record TokensDto(
            String access,
            String refresh
    ) {
    }

    public record CreateDto(Long id) {
    }
}
