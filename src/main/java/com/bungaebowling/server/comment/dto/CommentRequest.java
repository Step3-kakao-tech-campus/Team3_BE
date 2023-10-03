package com.bungaebowling.server.comment.dto;

import com.bungaebowling.server.comment.Comment;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.user.User;
import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    public record CreateDto(
            @NotBlank(message = "내용은 필수 입력 값 입니다.")
            String content
    ){
        public Comment createComment(User user, Post post) {
            return Comment.builder()
                    .post(post)
                    .user(user)
                    .content(content)
                    .build();
        }

        public Comment createReply(User user, Post post, Comment parent) {
            return Comment.builder()
                    .post(post)
                    .user(user)
                    .parent(parent)
                    .content(content)
                    .build();
        }
    }
}
