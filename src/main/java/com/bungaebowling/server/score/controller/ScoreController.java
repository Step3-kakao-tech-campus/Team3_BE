package com.bungaebowling.server.score.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server.score.dto.ScoreResponse;
import com.bungaebowling.server.score.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

//@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class ScoreController {

    private final ScoreService scoreService;

    @Autowired
    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping("/{postId}/scores")
    public ResponseEntity<?> getScores(@PathVariable Long postId) {
        ScoreResponse.GetScoresDto response = scoreService.readScores(postId);

        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    // multipart/form-data를 처리하고 json을 반환
    //@PostMapping(value = "/{postId}/scores", produces = "application/json", consumes = "multipart/form-data")
    @PostMapping(value = "/{postId}/scores", produces = "application/json", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestParam(name = "score") Integer scoreNum,
            @RequestParam(name = "image", required = false) MultipartFile image
    ) {
        scoreService.create(userDetails.getId(), postId, scoreNum, image);

        return ResponseEntity.ok().body(ApiUtils.success());
    }

    //@PutMapping(value = "/{postId}/scores/{scoreId}", produces = "application/json", consumes = "multipart/form-data")
    @PutMapping(value = "/{postId}/scores/{scoreId}", produces = "application/json", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long scoreId,
            @RequestParam(name = "score", required = false) Integer scoreNum,
            @RequestParam(name = "image", required = false) MultipartFile image
    ) {
        scoreService.update(userDetails.getId(), postId, scoreId, scoreNum, image);

        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @DeleteMapping("{postId}/scores/{scoreId}/image")
    public ResponseEntity<?> deleteScoreImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long scoreId
    ) {
        scoreService.deleteImage(userDetails.getId(), postId, scoreId);

        return ResponseEntity.ok(ApiUtils.success());
    }

    @DeleteMapping("{postId}/scores/{scoreId}")
    public ResponseEntity<?> deleteScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @PathVariable Long scoreId
    ) {
        scoreService.delete(userDetails.getId(), postId, scoreId);

        return ResponseEntity.ok(ApiUtils.success());
    }
}
