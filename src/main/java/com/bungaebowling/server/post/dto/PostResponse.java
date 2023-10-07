package com.bungaebowling.server.post.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.rate.UserRate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PostResponse {

    public record GetPostsDto(
        CursorRequest nextCursorRequest,
        List<PostDto> posts
    ) {
        public static GetPostsDto of(CursorRequest nextCursorRequest, List<Post> posts) {
            return new GetPostsDto(nextCursorRequest, posts.stream().map(PostDto::new).toList());
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
        public static GetParticipationRecordsDto of(CursorRequest nextCursorRequest, List<Post> posts,
                                                    Map<Long, List<Score>> scores, Map<Long, List<User>> members, Map<Long, List<UserRate>> rates) {
            return new GetParticipationRecordsDto(
                    nextCursorRequest,
                    posts.stream().map(post ->
                            new PostDto(
                                    post,
                                    scores.get(post.getId()),
                                    members.get(post.getId()),
                                    rates.get(post.getId())
                            )).toList()
            );
        }

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
            public PostDto(Post post, List<Score> scores, List<User> users, List<UserRate> rates) {
                this(
                        post.getId(),
                        post.getTitle(),
                        post.getDueTime(),
                        post.getDistrictName(),
                        post.getStartTime(),
                        post.getCurrentNumber(),
                        post.getIsClose(),
                        scores.stream().map(ScoreDto::new).toList(),
                        users.stream().map(user -> new MemberDto(user, rates)).toList()
                );
            }

            public record ScoreDto(
                    Long id,
                    Integer score,
                    String scoreImage
            ) {
                public ScoreDto(Score score) {
                    this(
                            score.getId(),
                            score.getScore(),
                            null //TODO: score image 추가
                    );
                }
            }

            public record MemberDto(
                    Long id,
                    String name,
                    String profileImage,
                    Boolean isRated
            ) {
                public MemberDto(User user, List<UserRate> rates) {
                    this(
                            user.getId(),
                            user.getName(),
                            user.getImgUrl(),
                            rates != null && rates.stream().anyMatch(rate -> rate.getUser().getId().equals(user.getId()))
                    );
                }
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
