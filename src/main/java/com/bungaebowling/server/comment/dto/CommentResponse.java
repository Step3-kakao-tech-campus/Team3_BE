package com.bungaebowling.server.comment.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.comment.Comment;
import com.bungaebowling.server.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CommentResponse {

    public record GetCommentsDto(
            CursorRequest nextCursorRequest,
            List<CommentDto> comments
    ) {

        public static GetCommentsDto of(CursorRequest nextCursorRequest, List<Comment> comments, List<List<Comment>> childComments) {
            return new GetCommentsDto(
                    nextCursorRequest,
                    IntStream.range(0, comments.size())
                            .mapToObj(index -> new CommentDto(comments.get(index), childComments.get(index)))
                            .toList()
            );
        }

        public record CommentDto(
                Long id,
                Long userId,
                String userName,
                String profileImage,
                String content,
                LocalDateTime createdAt,
                LocalDateTime editedAt,
                List<ChildCommentDto> childComments
        ) {

            public CommentDto(Comment comment, List<Comment> childComments) {
                this(
                        comment.getId(),
                        Optional.ofNullable(comment.getUser()).map(User::getId).orElse(null),
                        Optional.ofNullable(comment.getUser()).map(User::getName).orElse(null),
                        Optional.ofNullable(comment.getUser()).map(User::getImgUrl).orElse(null),
                        comment.getContent(),
                        comment.getCreatedAt(),
                        comment.getEditedAt(),
                        childComments.stream().map(ChildCommentDto::new).toList()
                );
            }

            public record ChildCommentDto(
                    Long id,
                    Long userId,
                    String userName,
                    String profileImage,
                    String content,
                    LocalDateTime createdAt,
                    LocalDateTime editedAt
            ) {

                public ChildCommentDto(Comment childComment) {
                    this(
                            childComment.getId(),
                            Optional.ofNullable(childComment.getUser()).map(User::getId).orElse(null),
                            Optional.ofNullable(childComment.getUser()).map(User::getName).orElse(null),
                            Optional.ofNullable(childComment.getUser()).map(User::getImgUrl).orElse(null),
                            childComment.getContent(),
                            childComment.getCreatedAt(),
                            childComment.getEditedAt()
                    );
                }
            }
        }

    }

    public record CreateDto(Long id) {
    }
}
