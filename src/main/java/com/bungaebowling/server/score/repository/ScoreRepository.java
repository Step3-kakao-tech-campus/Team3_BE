package com.bungaebowling.server.score.repository;

import com.bungaebowling.server.score.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findAllByPostId(Long postId);

    @Query("SELECT s FROM Score s WHERE s.post.id = :postId AND s.user.id = :userId")
    List<Score> findAllByPostIdAndUserId(Long postId, Long userId);

    List<Score> findAllByUserId(Long userId);

    @Query("SELECT s FROM Score s JOIN FETCH s.user u WHERE u.id = :userId and s.post.id = :postId ORDER BY s.id ASC")
    List<Score> findAllByUserIdAndPostIdOrderById(@Param("userId") Long userId, @Param("postId") Long postId);
}