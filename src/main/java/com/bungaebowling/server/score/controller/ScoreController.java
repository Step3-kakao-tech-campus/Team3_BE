package com.bungaebowling.server.score.controller;

import com.amazonaws.util.CollectionUtils;
import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server.score.Score;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class ScoreController {

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
