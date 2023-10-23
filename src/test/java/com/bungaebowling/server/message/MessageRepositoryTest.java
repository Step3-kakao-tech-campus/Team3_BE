package com.bungaebowling.server.message;

import com.bungaebowling.server.message.repository.MessageRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.List;

@DataJpaTest
@ActiveProfiles(value = {"test", "private", "aws"})
@Sql(value = "classpath:test_db/teardown.sql", config = @SqlConfig(encoding = "UTF-8"))
@DisplayName("쪽지 레포지토리 테스트")
class MessageRepositoryTest {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageRepositoryTest(MessageRepository messageRepository,
                                 UserRepository userRepository
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("나의 대화상대, 대화상대별 최신메시지 조회")
    void findLatestMessagesByUserId() {
        // given
        Long testUserId = 3L;
        // when
        System.out.println("====================start===================");
        List<Message> messages = messageRepository.findLatestMessagesPerOpponentByUserId(testUserId, null, Pageable.unpaged());
        System.out.println("========================end=====================");
        // then
        Assertions.assertThat(messages.get(0).getContent()).isEqualTo("4번이 3번에게 보낸 쪽지5");
        Assertions.assertThat(messages.get(0).getUser().getName()).isEqualTo("이볼링");
        Assertions.assertThat(messages.get(0).getOpponentUser().getName()).isEqualTo("박볼링");
        Assertions.assertThat(messages.get(0).getIsRead()).isFalse();
        Assertions.assertThat(messages.get(0).getIsReceive()).isTrue();
        Assertions.assertThat(messages.get(1).getContent()).isEqualTo("3번이 1번에게 보낸 쪽지5");
        Assertions.assertThat(messages.get(1).getUser().getName()).isEqualTo("이볼링");
        Assertions.assertThat(messages.get(1).getOpponentUser().getName()).isEqualTo("김볼링");
        Assertions.assertThat(messages.get(1).getIsRead()).isTrue();
        Assertions.assertThat(messages.get(1).getIsReceive()).isFalse();
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회")
    void findAllByUserIdAndOpponentUserOrderByIdDesc() {
        // given
        Long testUserId = 3L;
        Long opponentUserId = 1L;
        // when
        System.out.println("====================start===================");
        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testUserId, opponentUserId, null, Pageable.unpaged());
        System.out.println("========================end=====================");
        // then
        Assertions.assertThat(messages).hasSize(10);
        Assertions.assertThat(messages.get(0).getContent()).isEqualTo("3번이 1번에게 보낸 쪽지5");
        Assertions.assertThat(messages.get(0).getUser().getName()).isEqualTo("이볼링");
        Assertions.assertThat(messages.get(0).getOpponentUser().getName()).isEqualTo("김볼링");
    }

    @Test
    @DisplayName("대화방 쪽지 전체 삭제")
    void deleteAllByUserIdAndOpponentUserId() {
        // given
        Long testUserId = 3L;
        Long opponentUserId = 1L;
        int before = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testUserId, opponentUserId, null, Pageable.unpaged()).size();
        // when
        System.out.println("====================start===================");
        messageRepository.deleteAllByUserIdAndOpponentUserId(testUserId, opponentUserId);
        int after = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testUserId, opponentUserId, null, Pageable.unpaged()).size();
        System.out.println("========================end=====================");
        // then
        Assertions.assertThat(before).isEqualTo(10);
        Assertions.assertThat(after).isZero();
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회시 읽음 처리")
    void updateIsReadByUserIdAndOpponentUserId() {
        // given
        Long testUserId = 3L;
        Long opponentUserId = 4L;
        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testUserId, opponentUserId, null, Pageable.unpaged());
        long before = messages.stream()
                .filter(message -> !message.getIsRead())
                .count();
        // when
        System.out.println("====================start===================");
        messageRepository.updateIsReadTrueByUserIdAndOpponentUserId(testUserId, opponentUserId);
        List<Message> updatedMessages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testUserId, opponentUserId, null, Pageable.unpaged());
        long after = updatedMessages.stream()
                .filter(message -> !message.getIsRead())
                .count();
        System.out.println("========================end=====================");
        // then
        Assertions.assertThat(messages).hasSize(5);
        Assertions.assertThat(updatedMessages).hasSize(5);
        Assertions.assertThat(before).isEqualTo(5);
        Assertions.assertThat(after).isZero();
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회시 읽음 처리 영속성 테스트")
    void updateIsReadByUserIdAndOpponentUserIdContextTest() {
//        //given
//        User testuser = userRepository.findByName("테스트유저3").get();
//        User opponentUser = userRepository.findByName("테스트유저2").get();
//        //when
//        System.out.println("====================start===================");
//        messageRepository.updateIsReadTrueByUserIdAndOpponentUserId(testuser.getId(), opponentUser.getId());
//        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged());
//        long isReadCount = messages.stream()
//                .filter(message -> !message.getIsRead())
//                .count();
//        System.out.println("========================end=====================");
//        //then
//        Assertions.assertThat(messages.size()).isEqualTo(20);
//        Assertions.assertThat(isReadCount).isEqualTo(0);
    }
}