package com.bungaebowling.server.score.service;

import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.score.dto.ScoreResponse;
import com.bungaebowling.server.score.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ScoreService {
    private final ScoreRepository scoreRepository;

    public ScoreResponse.GetScoresDto readScores(Long postId) {
        List<Score> scores = findScores(postId);
        return ScoreResponse.GetScoresDto.of(scores);
    }

    private List<Score> findScores(Long postId) {
        return scoreRepository.findAllByPostId(postId);
    }
}
