package com.bungaebowling.server.post.dto;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;

import java.util.List;

public class PostResponse {

    public record GetPostsDto(
        CursorRequest nextCursorRequest,
        List<PostDto> posts
    ) {
        public record PostDto(
                Long id,
                String title,
                String dueTime,
                String districtName,
                String startTime,
                Integer averageScore,
                String userName,
                String profileImage,
                Boolean isClose
        ) {
        }

    }
}
