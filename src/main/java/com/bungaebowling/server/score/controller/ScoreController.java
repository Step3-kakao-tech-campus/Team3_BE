package com.bungaebowling.server.score.controller;

import com.amazonaws.util.CollectionUtils;
import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.score.dto.ScoreResponse;
import com.bungaebowling.server.score.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping("/{postId}/scores")
    public ResponseEntity<?> getScores(@PathVariable Long postId) {
        ScoreResponse.GetScoresDto response = scoreService.readScores(postId);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    // multipart/form-data를 처리하고 json을 반환
    @RequestMapping(value = "/{postId}/scores", produces = "application/json", consumes = "multipart/form-data")
    public ResponseEntity<?> createScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam(name = "scores") List<Integer> scores,
            @RequestParam(name = "images") List<MultipartFile> images
    ) {
        if (CollectionUtils.isNullOrEmpty(scores)) { // null 체크도 해즘
            throw new Exception400("점수를 입력해주세요.");
        }

        if (CollectionUtils.isNullOrEmpty(images)) { // null 체크도 해즘
            throw new Exception400("점수 사진을 등록해주세요.");
        }

        scoreService.create(userDetails.getId(), postId, scores, images);

        return ResponseEntity.ok().body(ApiUtils.success());
    }


}
