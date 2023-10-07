package com.bungaebowling.server.score.repository;

import com.bungaebowling.server.score.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findAllByPostId(Long postId);
}
