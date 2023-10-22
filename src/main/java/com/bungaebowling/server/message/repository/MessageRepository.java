package com.bungaebowling.server.message.repository;

import com.bungaebowling.server.message.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m " +
            "FROM Message m " +
            "JOIN FETCH m.opponentUser oppUser " +
            "WHERE m.id IN (SELECT MAX(m2.id) FROM Message m2 WHERE m2.user.id = :userId " +
            "GROUP BY m2.opponentUser.id) " +
            "And (:key IS NULL OR m.id < :key)" +
            "ORDER BY m.id DESC")
    List<Message> findLatestMessagesPerOpponentByUserId(@Param("userId") Long userId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT m.opponentUser.id, COUNT(m) " +
            "FROM Message m " +
            "WHERE m.user.id = :userId AND m.isRead = false And m.isReceive = true " +
            "And (:key IS NULL OR m.id < :key) " +
            "GROUP BY m.opponentUser.id " +
            "ORDER BY MAX(m.id) DESC")
    List<Long[]> countUnreadMessagesWithOpponents(@Param("userId") Long userId, @Param("key") Long key, Pageable pageable);

    @Query("SELECT m " +
            "FROM Message m " +
            "WHERE (m.user.id = :userId AND m.opponentUser.id = :opponentId) " +
            "AND (:key IS NULL OR m.id < :key) " +
            "ORDER BY m.id DESC")
    List<Message> findAllByUserIdAndOpponentUserIdOrderByIdDesc(@Param("userId") Long userId, @Param("opponentId") Long opponentId, @Param("key") Long key, Pageable pageable);

    void deleteAllByUserIdAndOpponentUserId(Long userId, Long opponentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.isRead = true WHERE m.user.id = :userId AND m.opponentUser.id = :opponentId AND m.isRead = false")
    void updateIsReadTrueByUserIdAndOpponentUserId(Long userId, Long opponentId);
}
