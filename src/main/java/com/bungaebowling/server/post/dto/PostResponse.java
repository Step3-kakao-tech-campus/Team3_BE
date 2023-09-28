package com.bungaebowling.server.post.dto;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.post.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostResponse {

    public record GetPostsDto(
        CursorRequest nextCursorRequest,
        List<PostDto> posts
    ) {
        public static GetPostsDto of(CursorRequest nextCursorRequest, List<Post> posts) {
            return new GetPostsDto(nextCursorRequest, posts.stream().map(PostDto::new).collect(Collectors.toList()));
        }

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
            public PostDto(Post post) {
                this(
                        post.getId(),
                        post.getTitle(),
                        post.getDueTime(),
                        post.getDistrictName(),
                        post.getStartTime(),
                        post.getUserName(),
                        post.getProfilePath(),
                        post.getCurrentNumber(),
                        post.getIsClose()
                );
            }
        }
    }

    public record GetPostDto(
            PostDto post
    ) {
        public GetPostDto(Post post) {
            this(new PostDto(post));
        }
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
            public PostDto(Post post) {
                this(
                        post.getId(),
                        post.getTitle(),
                        post.getUserName(),
                        post.getProfilePath(),
                        post.getDistrictName(),
                        post.getCurrentNumber(),
                        post.getContent(),
                        post.getStartTime(),
                        post.getDueTime(),
                        post.getViewCount(),
                        post.getCreatedAt(),
                        post.getEditedAt(),
                        post.getIsClose()
                );
            }
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
