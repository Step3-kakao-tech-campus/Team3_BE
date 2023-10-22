package com.bungaebowling.server.message;

import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.message.repository.MessageRepository;
import com.bungaebowling.server.user.Role;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@Import(ObjectMapper.class)
@DisplayName("쪽지 레포지토리 테스트")
class MessageRepositoryTest {

    private final MessageRepository messageRepository;
    private final EntityManager em;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final ObjectMapper om;

    @Autowired
    public MessageRepositoryTest(MessageRepository messageRepository,
                                 EntityManager em,
                                 UserRepository userRepository,
                                 DistrictRepository districtRepository,
                                 ObjectMapper om
    ) {
        this.messageRepository = messageRepository;
        this.em = em;
        this.userRepository = userRepository;
        this.districtRepository = districtRepository;
        this.om = om;
    }

    @BeforeEach
    @DisplayName("초기세팅- 유저 등록, 쪽지 생성")
    public void setUp() {
        District district = districtRepository.findById(1l).get();
        User testuser1 = User.builder()
                .name("테스트유저1")
                .email("test_1@test.com")
                .district(district)
                .imgUrl("testimage1")
                .role(Role.ROLE_USER)
                .password("testpassword1")
                .build();
        User testuser2 = User.builder()
                .name("테스트유저2")
                .email("test2@test.com")
                .district(district)
                .imgUrl("testimage2")
                .role(Role.ROLE_USER)
                .password("testpassword1")
                .build();
        User testuser3 = User.builder()
                .name("테스트유저3")
                .email("test3@test.com")
                .district(district)
                .imgUrl("testimage3")
                .role(Role.ROLE_USER)
                .password("testpassword1")
                .build();

        userRepository.save(testuser1);
        userRepository.save(testuser2);
        userRepository.save(testuser3);
        userRepository.findByName("테스트유저1").get();
        List<Message> messages = new ArrayList<>();

        //테스트유저1 ->테스트유저2
        for (int i = 0; i < 10; i++) {
            Message userMessage = Message.builder()
                    .user(testuser1)
                    .opponentUser(testuser2)
                    .content(Integer.toString(i))
                    .isReceive(false)
                    .isRead(true)
                    .build();
            Message opponentMessage = Message.builder()
                    .user(testuser2)
                    .opponentUser(testuser1)
                    .content(Integer.toString(i))
                    .isReceive(true)
                    .isRead(false)
                    .build();
            messages.add(userMessage);
            messages.add(opponentMessage);
        }
        //테스트유저1 ->테스트유저3
        for (int i = 10; i < 20; i++) {
            Message userMessage = Message.builder()
                    .user(testuser1)
                    .opponentUser(testuser3)
                    .content(Integer.toString(i))
                    .isReceive(false)
                    .isRead(true)
                    .build();
            Message opponentMessage = Message.builder()
                    .user(testuser3)
                    .opponentUser(testuser1)
                    .content(Integer.toString(i))
                    .isReceive(true)
                    .isRead(false)
                    .build();
            messages.add(userMessage);
            messages.add(opponentMessage);
        }
        //테스트유저2 ->테스트유저3
        for (int i = 20; i < 30; i++) {
            Message userMessage = Message.builder()
                    .user(testuser2)
                    .opponentUser(testuser3)
                    .content(Integer.toString(i))
                    .isReceive(false)
                    .isRead(true)
                    .build();
            Message opponentMessage = Message.builder()
                    .user(testuser3)
                    .opponentUser(testuser2)
                    .content(Integer.toString(i))
                    .isReceive(true)
                    .isRead(false)
                    .build();
            messages.add(userMessage);
            messages.add(opponentMessage);
        }
        //테스트유저3 ->테스트유저2
        for (int i = 30; i < 40; i++) {
            Message userMessage = Message.builder()
                    .user(testuser3)
                    .opponentUser(testuser2)
                    .content(Integer.toString(i))
                    .isReceive(false)
                    .isRead(true)
                    .build();
            Message opponentMessage = Message.builder()
                    .user(testuser2)
                    .opponentUser(testuser3)
                    .content(Integer.toString(i))
                    .isReceive(true)
                    .isRead(false)
                    .build();
            messages.add(userMessage);
            messages.add(opponentMessage);
        }

        messageRepository.saveAll(messages);
    }

    @Test
    @DisplayName("나의 대화상대, 대화상대별 최신메시지 조회")
    void findLatestMessagesByUserId() {
        //given
        User testuser = userRepository.findByName("테스트유저3").get();
        //when
        System.out.println("====================start===================");
        List<Message> messages = messageRepository.findLatestMessagesPerOpponentByUserId(testuser.getId(), null, Pageable.unpaged());
        System.out.println("========================end=====================");
        //then
        Assertions.assertThat(messages.get(0).getContent()).isEqualTo("39");
        Assertions.assertThat(messages.get(0).getUser().getName()).isEqualTo("테스트유저3");
        Assertions.assertThat(messages.get(0).getOpponentUser().getName()).isEqualTo("테스트유저2");
        Assertions.assertThat(messages.get(0).getIsRead()).isEqualTo(true);
        Assertions.assertThat(messages.get(0).getIsReceive()).isEqualTo(false);
        Assertions.assertThat(messages.get(0).getOpponentUser().getImgUrl()).isEqualTo("testimage2");
        Assertions.assertThat(messages.get(1).getContent()).isEqualTo("19");
        Assertions.assertThat(messages.get(1).getUser().getName()).isEqualTo("테스트유저3");
        Assertions.assertThat(messages.get(1).getOpponentUser().getName()).isEqualTo("테스트유저1");
        Assertions.assertThat(messages.get(1).getIsRead()).isEqualTo(false);
        Assertions.assertThat(messages.get(1).getIsReceive()).isEqualTo(true);
        Assertions.assertThat(messages.get(1).getOpponentUser().getImgUrl()).isEqualTo("testimage1");
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회")
    void findAllByUserIdAndOpponentUserOrderByIdDesc() {
        //given
        User testuser = userRepository.findByName("테스트유저3").get();
        User opponentUser = userRepository.findByName("테스트유저2").get();
        //when
        System.out.println("====================start===================");
        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged());
        System.out.println("========================end=====================");
        //then
        Assertions.assertThat(messages.size()).isEqualTo(20);
        Assertions.assertThat(messages.get(0).getContent()).isEqualTo("39");
        Assertions.assertThat(messages.get(0).getUser().getName()).isEqualTo("테스트유저3");
        Assertions.assertThat(messages.get(0).getOpponentUser().getName()).isEqualTo("테스트유저2");
        Assertions.assertThat(messages.get(0).getOpponentUser().getImgUrl()).isEqualTo("testimage2");
    }

    @Test
    @DisplayName("대화방 쪽지 전체 삭제")
    void deleteAllByUserIdAndOpponentUserId() {
        //given
        User testuser = userRepository.findByName("테스트유저3").get();
        User opponentUser = userRepository.findByName("테스트유저2").get();
        int before = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged()).size();
        //when
        System.out.println("====================start===================");
        messageRepository.deleteAllByUserIdAndOpponentUserId(testuser.getId(), opponentUser.getId());
        int after = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged()).size();
        System.out.println("========================end=====================");
        //then
        Assertions.assertThat(before).isEqualTo(20);
        Assertions.assertThat(after).isEqualTo(0);
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회시 읽음 처리")
    void updateIsReadByUserIdAndOpponentUserId() {
        //given
        User testuser = userRepository.findByName("테스트유저3").get();
        User opponentUser = userRepository.findByName("테스트유저2").get();
        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged());
        long before = messages.stream()
                .filter(message -> !message.getIsRead())
                .count();
        //when
        System.out.println("====================start===================");
        messageRepository.updateIsReadTrueByUserIdAndOpponentUserId(testuser.getId(), opponentUser.getId());
        List<Message> updatedMessages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged());
        long after = updatedMessages.stream()
                .filter(message -> !message.getIsRead())
                .count();
        System.out.println("========================end=====================");
        //then
        Assertions.assertThat(messages.size()).isEqualTo(20);
        Assertions.assertThat(updatedMessages.size()).isEqualTo(20);
        Assertions.assertThat(before).isEqualTo(10);
        Assertions.assertThat(after).isEqualTo(0);
    }

    @Test
    @DisplayName("일대일 대화방 쪽지 조회시 읽음 처리 영속성 테스트")
    void updateIsReadByUserIdAndOpponentUserIdContextTest() {
        //given
        User testuser = userRepository.findByName("테스트유저3").get();
        User opponentUser = userRepository.findByName("테스트유저2").get();
        //when
        System.out.println("====================start===================");
        messageRepository.updateIsReadTrueByUserIdAndOpponentUserId(testuser.getId(), opponentUser.getId());
        List<Message> messages = messageRepository.findAllByUserIdAndOpponentUserIdOrderByIdDesc(testuser.getId(), opponentUser.getId(), null, Pageable.unpaged());
        long isReadCount = messages.stream()
                .filter(message -> !message.getIsRead())
                .count();
        System.out.println("========================end=====================");
        //then
        Assertions.assertThat(messages.size()).isEqualTo(20);
        Assertions.assertThat(isReadCount).isEqualTo(0);
    }
}