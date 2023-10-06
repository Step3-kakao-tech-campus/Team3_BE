package com.bungaebowling.server.score.dto;

import com.bungaebowling.server.score.Score;

import java.util.List;
import java.util.stream.Collectors;

public class ScoreResponse {
    public record GetScoresDto(
            List<ScoreDto> scores
    ) {
        public static GetScoresDto of(List<Score> scores) {
            return new GetScoresDto(scores.stream().map(ScoreDto::new).collect(Collectors.toList()));
        }
        public record ScoreDto(
                Long id,
                Integer score,
                String scoreImage
        ) {
            public ScoreDto(Score score) {
                this(
                        score.getId(),
                        score.getScore(),
                        score.getResultImageUrl()
                );
            }
        }
    }
}
