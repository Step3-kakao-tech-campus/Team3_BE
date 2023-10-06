package com.bungaebowling.server.message.service;
import com.bungaebowling.server._core.errors.exception.client.Exception400;
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


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    public static final int DEFAULT_SIZE= 20;

    public MessageResponse.GetOpponentsDto getOpponents(CursorRequest cursorRequest, Long opponentId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);
        userRepository.findById(opponentId).orElseThrow(()->new Exception400("존재하지 않는 유저입니다."));
        List<Message> messages = messageRepository.findLatestMessagesPerOpponentByUserId(opponentId, cursorRequest.key(), pageable);
        Long countNew = messageRepository.countByUserIdAndIsReceiveTrueAndIsReadFalse(opponentId);
        Long countAll = messageRepository.countByUserIdAndIsReceiveTrue(opponentId);
        Long lastKey = messages.isEmpty() ? CursorRequest.NONE_KEY : messages.get(messages.size()-1).getId();
        return MessageResponse.GetOpponentsDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE),countNew,countAll,messages);
    }


    @Transactional
    public MessageResponse.GetMessagesDto getMessagesByOpponentId(CursorRequest cursorRequest, Long userId, Long opponentId) {
        if (userId.equals(opponentId)){throw new Exception400("자신에게는 쪽지를 보낼 수 없습니다.");}
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);
        User opponentUser = userRepository.findById(opponentId).orElseThrow(()->new Exception400("존재하지 않는 유저입니다."));
        List<Message> messages= messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(userId,opponentId,cursorRequest.key(), pageable);
        messages.stream().forEach(m-> m.read());
        Long lastKey = messages.isEmpty() ? CursorRequest.NONE_KEY : messages.get(messages.size()-1).getId();
        return MessageResponse.GetMessagesDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE),messages,opponentUser);
    }

    @Transactional
    public void sendMessage(MessageRequest.SendMessageDto requestDto, Long userId, Long opponentId) {
        if (userId.equals(opponentId)){throw new Exception400("자신과 쪽지 대화를 할 수 없습니다.");}
        User user = userRepository.findById(userId).orElseThrow(()->new Exception400("존재하지 않는 유저입니다."));
        User opponentUser = userRepository.findById(opponentId).orElseThrow(()->new Exception400("존재하지 않는 유저입니다."));
        Message userMessage = requestDto.toEntity(user,opponentUser,false,true);
        Message opponentMessage = requestDto.toEntity(opponentUser,user,true,false);
        messageRepository.save(userMessage);
        messageRepository.save(opponentMessage);
    }

    @Transactional
    public void deleteMessagesByOpponentId(Long userId, Long opponentId) {
        userRepository.findById(userId).orElseThrow(()->new Exception400("존재하지 않는 유저입니다."));
        messageRepository.deleteAllByUserIdAndOpponentUserId(userId,opponentId);
    }
    
    @Transactional
    public void deleteMessageById(Long userId, Long messageId) {
        Message message = messageRepository.findById(messageId).orElseThrow(()-> new Exception400("존재하지 않는 쪽지입니다."));
        if (!userId.equals(message.getUser().getId())){
            throw new Exception400("해당 유저의 쪽지가 아닙니다.");
        }
        messageRepository.deleteById(messageId);
    }
}
