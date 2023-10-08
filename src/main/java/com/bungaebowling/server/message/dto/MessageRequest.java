package com.bungaebowling.server.message.dto;
import com.bungaebowling.server.message.Message;
import com.bungaebowling.server.user.User;
import jakarta.validation.constraints.NotBlank;

public class MessageRequest {
    public record SendMessageDto (
            @NotBlank(message = "쪽지 내용은 필수 입력 사항입니다.")
            String content

    ){
        public Message toEntity(User user, User opponentUser, Boolean isReceive, Boolean isRead) {
            return Message.builder()
                    .user(user)
                    .opponentUser(opponentUser)
                    .isRead(isRead)
                    .isReceive(isReceive)
                    .content(content)
                    .build();
        }
    }
}
