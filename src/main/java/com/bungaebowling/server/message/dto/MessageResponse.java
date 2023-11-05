package com.bungaebowling.server.message.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.message.Message;
import com.bungaebowling.server.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MessageResponse {
    public record GetOpponentsDto(
            CursorRequest nextCursorRequest,
            List<MessageResponse.GetOpponentsDto.MessageDto> messages
    ) {
        public static MessageResponse.GetOpponentsDto of(CursorRequest nextCursorRequest, List<Message> messages, Map<Long, Long> countNews) {

            return new MessageResponse.GetOpponentsDto(
                    nextCursorRequest,
                    messages.stream()
                            .map(message -> new MessageDto(message, countNews.getOrDefault(message.getOpponentUser().getId(), 0L)))
                            .toList()
            );
        }

        public record MessageDto(
                Long opponentUserId,
                String opponentUserName,
                String opponentUserProfileImage,
                String recentMessage,
                LocalDateTime recentTime,
                Long countNew
        ) {
            public MessageDto(Message message, Long countNew) {
                this(
                        message.getOpponentUser().getId(),
                        message.getOpponentUser().getName(),
                        message.getOpponentUser().getImgUrl(),
                        message.getContent(),
                        message.getCreatedAt(),
                        countNew
                );
            }
        }
    }

    public record GetMessagesDto(
            CursorRequest nextCursorRequest,
            String opponentUserName,
            String opponentUserProfileImage,
            List<MessageResponse.GetMessagesDto.MessageDto> messages
    ) {
        public static MessageResponse.GetMessagesDto of(CursorRequest nextCursorRequest, List<Message> messages, User opponentUser) {
            return new MessageResponse.GetMessagesDto(nextCursorRequest, opponentUser.getName(), opponentUser.getImgUrl(), messages.stream().map(MessageResponse.GetMessagesDto.MessageDto::new).toList());
        }

        public record MessageDto(
                Long id,
                String content,
                LocalDateTime time,
                Boolean isRead,
                Boolean isReceive
        ) {
            public MessageDto(Message message) {
                this(
                        message.getId(),
                        message.getContent(),
                        message.getCreatedAt(),
                        message.getIsRead(),
                        message.getIsReceive()
                );
            }
        }
    }
}
