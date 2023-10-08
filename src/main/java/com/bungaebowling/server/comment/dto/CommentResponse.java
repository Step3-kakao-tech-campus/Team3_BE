package com.bungaebowling.server.comment.dto;

import com.bungaebowling.server._core.utils.CursorRequest;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {

    public record GetCommentsDto(
            CursorRequest nextCursorRequest,
            List<CommentDto> comments
    ) {
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
            public record ChildCommentDto(
                    Long id,
                    Long userId,
                    String userName,
                    String profileImage,
                    String content,
                    LocalDateTime createdAt,
                    LocalDateTime editedAt
            ) {

            }
        }

    }
}
