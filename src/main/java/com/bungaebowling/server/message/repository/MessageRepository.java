package com.bungaebowling.server.message.repository;
import com.bungaebowling.server.message.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {



    @Query("SELECT m " +
            "FROM Message m " +
            "JOIN FETCH m.opponentUser oppUser " +
            "WHERE m.id " +
            "IN (SELECT MAX(m2.id) FROM Message m2 WHERE m2.user.id = :userId GROUP BY m2.opponentUser.id) " +
            "And (m.id < :key OR :key IS NULL)"+
            "ORDER BY m.id DESC")
    List<Message> findLatestMessagesPerOpponentByUserId(@Param("userId") Long userId,@Param("key") Long key, Pageable pageable);



    @Query("SELECT count(m) " +
            "FROM Message m " +
            "WHERE m.user.id = :userId AND m.isRead = false And m.isReceive = true ")
    Long countByUserIdAndIsReceiveTrueAndIsReadFalse(@Param("userId") Long userId);

    @Query("SELECT count(m) " +
            "FROM Message m " +
            "WHERE m.user.id = :userId And m.isReceive = true ")
    Long countByUserIdAndIsReceiveTrue(@Param("userId") Long userId);


    @Query("SELECT m " +
            "FROM Message m " +
            "WHERE (m.user.id = :userId AND m.opponentUser.id = :opponentId) " +
            "AND (m.id < :key OR :key IS NULL) " +
            "ORDER BY m.id DESC")
    List<Message> findAllByUserIdAndOpponentUserIdOrderByIdDesc(@Param("userId") Long userId, @Param("opponentId") Long opponentId, @Param("key") Long key, Pageable pageable);


    void deleteAllByUserIdAndOpponentUserId(Long userId, Long opponentId);

    List<Message> findAllByUserId(Long userId);


}
