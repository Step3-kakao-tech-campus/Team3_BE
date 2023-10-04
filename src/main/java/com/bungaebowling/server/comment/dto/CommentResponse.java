package com.bungaebowling.server.comment.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.comment.Comment;

import java.time.LocalDateTime;
import java.util.List;
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
                        comment.getUser().getId(),
                        comment.getUser().getName(),
                        comment.getUser().getImgUrl(),
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
                            childComment.getUser().getId(),
                            childComment.getUser().getName(),
                            childComment.getUser().getImgUrl(),
                            childComment.getContent(),
                            childComment.getCreatedAt(),
                            childComment.getEditedAt()
                    );
                }
            }
        }

    }

    public record CreateDto(Long id) { }
}
