package com.bungaebowling.server.post.dto;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    public record GetPostsDto(
        CursorRequest nextCursorRequest,
        List<PostDto> posts
    ) {
        public record PostDto(
                Long id,
                String title,
                LocalDateTime dueTime,
                String districtName,
                LocalDateTime startTime,
                String userName,
                String profileImage,
                Integer currentNumber,
                Boolean isClose
        ) {
        }
    }

    public record GetPostDto(
            PostDto post
    ) {
        public record PostDto(
                Long id,
                String title,
                String userName,
                String profileImage,
                String districtName,
                Integer currentNumber,
                String content,
                LocalDateTime startTime,
                LocalDateTime dueTime,
                Integer viewCount,
                LocalDateTime createdAt,
                LocalDateTime editedAt,
                Boolean isClose
        ) {
        }
    }

    public record GetParticipationRecordsDto(
            CursorRequest nextCursorRequest,
            List<PostDto> posts
    ) {
        public record PostDto(
                Long id,
                String title,
                LocalDateTime dueTime,
                String districtName,
                LocalDateTime startTime,
                Integer currentNumber,
                Boolean isClose,
                List<ScoreDto> scores,
                List<MemberDto> members
        ) {
            public record ScoreDto(
                    Long id,
                    Integer score,
                    String scoreImage
            ) {

            }

            public record MemberDto(
                    Long id,
                    String name,
                    String profileImage,
                    Boolean isRated
            ) {

            }
        }
    }

    public record GetScoresDto(
            List<ScoreDto> scores
    ) {
        public record ScoreDto(
                Long id,
                Integer score,
                String scoreImage
        ) {

        }
    }
}
