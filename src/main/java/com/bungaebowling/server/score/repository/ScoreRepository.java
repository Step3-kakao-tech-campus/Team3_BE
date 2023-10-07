package com.bungaebowling.server.score.repository;

import com.bungaebowling.server.score.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findAllByUserId(Long userId);

    @Query("SELECT MAX(s.score) FROM Score s WHERE s.user.id = :userId")
    Integer findMaxScoreByUserId(@Param("userId") Long userId);

    @Query("SELECT MIN(s.score) FROM Score s WHERE s.user.id = :userId")
    Integer findMinScoreByUserId(@Param("userId") Long userId);
}
