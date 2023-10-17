package com.bungaebowling.server.message.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.message.Message;
import com.bungaebowling.server.message.dto.MessageRequest;
import com.bungaebowling.server.message.dto.MessageResponse;
import com.bungaebowling.server.message.repository.MessageRepository;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    public static final int DEFAULT_SIZE = 20;

    public MessageResponse.GetOpponentsDto getOpponents(CursorRequest cursorRequest, Long userId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);

        getUser(userId);

        List<Message> messages = messageRepository.findLatestMessagesPerOpponentByUserId(userId, cursorRequest.key(), pageable);

        Map<Long, Long> countNews = messageRepository.countUnreadMessagesWithOpponents(userId, cursorRequest.key(), pageable)
                .stream()
                .collect(Collectors.toMap(
                        row -> row[0],
                        row -> row[1]
                ));

        Long lastKey = messages.isEmpty() ? CursorRequest.NONE_KEY : messages.get(messages.size() - 1).getId();
        return MessageResponse.GetOpponentsDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), messages, countNews);
    }

    @Transactional
    public MessageResponse.GetMessagesDto getMessagesAndUpdateToRead(CursorRequest cursorRequest, Long userId, Long opponentId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);

        userCheck(userId, opponentId);

        User opponentUser = getUser(opponentId);
        messageRepository.updateIsReadTrueByUserIdAndOpponentUserId(userId, opponentId);

        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(userId, opponentId, cursorRequest.key(), pageable);

        Long lastKey = messages.isEmpty() ? CursorRequest.NONE_KEY : messages.get(messages.size() - 1).getId();
        return MessageResponse.GetMessagesDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), messages, opponentUser);
    }

    @Transactional
    public void sendMessage(MessageRequest.SendMessageDto requestDto, Long userId, Long opponentId) {
        userCheck(userId, opponentId);
        User user = getUser(userId);
        User opponentUser = getUser(opponentId);

        Message userMessage = requestDto.toEntity(user, opponentUser, false, true);
        Message opponentMessage = requestDto.toEntity(opponentUser, user, true, false);
        messageRepository.save(userMessage);
        messageRepository.save(opponentMessage);
    }

    @Transactional
    public void deleteMessagesByOpponentId(Long userId, Long opponentId) {
        userCheck(userId, opponentId);
        getUser(userId);

        messageRepository.deleteAllByUserIdAndOpponentUserId(userId, opponentId);
    }

    @Transactional
    public void deleteMessageById(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));
        if (!userId.equals(message.getUser().getId())) {
            throw new CustomException(ErrorCode.MESSAGE_DELETE_FAILED);
        }

        messageRepository.deleteById(messageId);
    }

    public void userCheck(Long userId, Long opponentId) {
        if (userId.equals(opponentId)) {
            throw new CustomException(ErrorCode.MESSAGE_NOT_ALLOWED);
        }
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
