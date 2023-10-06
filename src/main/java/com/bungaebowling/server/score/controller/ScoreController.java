package com.bungaebowling.server.score.controller;

import com.amazonaws.util.CollectionUtils;
import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.score.dto.ScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class ScoreController {

    @GetMapping("/{postId}/scores")
    public ResponseEntity<?> getPostScores(@PathVariable Long postId) {
        List<ScoreResponse.GetScoresDto.ScoreDto> scoreDtos = new ArrayList<>();
        var getScoreDto1 = new ScoreResponse.GetScoresDto.ScoreDto(
                1L,
                180,
                "/scoreImages/1.jpg"
        );
        scoreDtos.add(getScoreDto1);
        var getScoreDto2 = new ScoreResponse.GetScoresDto.ScoreDto(
                2L,
                210,
                null
        );
        scoreDtos.add(getScoreDto2);

        var response = ApiUtils.success(scoreDtos);
        return ResponseEntity.ok().body(response);
    }

    // multipart/form-data를 처리하고 json을 반환
    @RequestMapping(value = "/{postId}/scores", produces = "application/json", consumes = "multipart/form-data")
    public ResponseEntity<?> createScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam(name = "scores") List<Integer> score,
            @RequestParam(name = "images") List<Score> images
    ) {
        if (CollectionUtils.isNullOrEmpty(score)) { // null 체크도 해즘
            throw new Exception400("점수를 입력해주세요.");
        }

        if (CollectionUtils.isNullOrEmpty(images)) { // null 체크도 해즘
            throw new Exception400("점수 사진을 등록해주세요.");
        }

    }


}
