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
        public static GetPostsDto of(
                CursorRequest nextCursorRequest,
                List<Post> posts,
                Map<Long, Long> currentNumberMap
        ) {
            return new GetPostsDto(
                    nextCursorRequest,
                    posts.stream().map(post ->
                            new PostDto(
                                    post,
                                    currentNumberMap.get(post.getId()))
                    ).toList()
            );
        }

        public record PostDto(
                Long id,
                String title,
                LocalDateTime dueTime,
                String districtName,
                LocalDateTime startTime,
                String userName,
                String profileImage,
                Long currentNumber,
                Boolean isClose
        ) {
            public PostDto(Post post, Long currentNumber) {
                this(
                        post.getId(),
                        post.getTitle(),
                        post.getDueTime(),
                        post.getDistrictName(),
                        post.getStartTime(),
                        post.getUserName(),
                        post.getProfilePath(),
                        currentNumber,
                        post.getIsClose()
                );
            }
        }
    }

    public record GetPostDto(
            PostDto post
    ) {
        public GetPostDto(Post post, Long currentNumber) {
            this(new PostDto(post, currentNumber));
        }

        public record PostDto(
                Long id,
                String title,
                Long userId,
                String userName,
                String profileImage,
                String districtName,
                Long currentNumber,
                String content,
                LocalDateTime startTime,
                LocalDateTime dueTime,
                Integer viewCount,
                LocalDateTime createdAt,
                LocalDateTime editedAt,
                Boolean isClose
        ) {
            public PostDto(Post post, Long currentNumber) {
                this(
                        post.getId(),
                        post.getTitle(),
                        post.getUser().getId(),
                        post.getUserName(),
                        post.getProfilePath(),
                        post.getDistrictName(),
                        currentNumber,
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
        public static GetParticipationRecordsDto of(
                CursorRequest nextCursorRequest,
                List<Post> posts,
                Map<Long, List<Score>> scores,
                Map<Long, List<User>> members,
                Map<Long, List<UserRate>> rates,
                Map<Long, Long> applicantIdMap,
                Map<Long, Long> currentNumberMap,
                Map<Long, String> districtNameMap
        ) {
            return new GetParticipationRecordsDto(
                    nextCursorRequest,
                    posts.stream().map(post ->
                            new PostDto(
                                    post,
                                    scores.get(post.getId()),
                                    members.get(post.getId()),
                                    rates.get(post.getId()),
                                    applicantIdMap.get(post.getId()),
                                    currentNumberMap.get(post.getId()),
                                    districtNameMap.get(post.getId())
                            )).toList()
            );
        }

        public record PostDto(
                Long id,
                Long applicantId,
                String title,
                LocalDateTime dueTime,
                String districtName,
                LocalDateTime startTime,
                Long currentNumber,
                Boolean isClose,
                List<ScoreDto> scores,
                List<MemberDto> members
        ) {
            public PostDto(
                    Post post,
                    List<Score> scores,
                    List<User> users,
                    List<UserRate> rates,
                    Long applicantId,
                    Long currentNumber,
                    String districtName
            ) {
                this(
                        post.getId(),
                        applicantId,
                        post.getTitle(),
                        post.getDueTime(),
                        districtName,
                        post.getStartTime(),
                        currentNumber,
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
                            score.getScoreNum(),
                            score.getAccessImageUrl()
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
                            rates.stream().anyMatch(rate -> rate.getUser().getId().equals(user.getId()))
                    );
                }
            }
        }
    }

    // Post시 postId 반환용
    public record CreateDto(Long id) {
    }
}
