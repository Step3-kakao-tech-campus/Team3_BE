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

    List<Score> findAllByUserId(Long userId);

    List<Score> findAllByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT MAX(s.scoreNum) FROM Score s WHERE s.user.id = :userId")
    Integer findMaxScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT MIN(s.scoreNum) FROM Score s WHERE s.user.id = :userId")
    Integer findMinScoreByUserId(@Param("userId") Long userId);
}
