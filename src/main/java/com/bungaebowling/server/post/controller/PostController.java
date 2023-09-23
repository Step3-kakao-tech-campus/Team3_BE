package com.bungaebowling.server.post.controller;

import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @GetMapping
    public ResponseEntity<?> getPosts() {
        CursorRequest cursorRequest = new CursorRequest(1L, 20);
        List<PostResponse.GetPostsDto.PostDto> postDtos = new ArrayList<>();
        var postDto1 = new PostResponse.GetPostsDto.PostDto(
                1L,
                "오늘 7시에 부산대 락볼링장에서 게임하실분~~",
                "9월 7일 (목) 오후 9:00",
                "부산광역시 금정구 장전2동",
                "9월 9일 (토) 오전 9:00",
                "김볼링",
                null,
                4,
                false
        );
        postDtos.add(postDto1);
        var postDto2 = new PostResponse.GetPostsDto.PostDto(
                2L,
                "오늘 당장 나올사람!",
                "9월 7일 (목) 오후 8:00",
                "부산광역시 동래구",
                "9월 7일 (목) 오후 8:30",
                "최볼링",
                null,
                2,
                false
        );
        postDtos.add(postDto2);
        var postDto3 = new PostResponse.GetPostsDto.PostDto(
                3L,
                "오늘 7시에 부산대 락볼링장에서 게임하실분~~",
                "9월 7일 (목) 오후 3:00",
                "부산광역시 부산진구 부전동",
                "9월 8일 (금) 오후 7:00",
                "이볼링",
                null,
                1,
                false
        );
        postDtos.add(postDto3);

        var getPostsDto = new PostResponse.GetPostsDto(cursorRequest, postDtos);

        var response = ApiUtils.success(getPostsDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        var getPostDto = new PostResponse.GetPostDto(
                new PostResponse.GetPostDto.PostDto(
                        postId,
                        "오늘 7시에 부산대 락볼링장에서 게임하실분~~",
                        "김볼링",
                        null,
                        "부산광역시 금정구 장전2동",
                        1,
                        "오늘 오후 7시에 부산대 락볼링장에서 게임하실 분 구합니다.\n즐겜 할거구여 초보자 환영합니다. 저도 볼링 세 번 밖에 안쳐봤어요 ㅎㅎ\n연락주세요",
                        "9월 7일 (목) 오후 9:00",
                        "9월 9일 (토) 오전 9:00",
                        100,
                        "2023-09-06 21:00",
                        "2023-09-06 21:00",
                        false
                )
        );

        var response = ApiUtils.success(getPostDto);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{postId}/scores")
    public ResponseEntity<?> getPostScores(@PathVariable Long postId) {
        List<PostResponse.GetScoresDto.ScoreDto> scoreDtos = new ArrayList<>();
        var getScoreDto1 = new PostResponse.GetScoresDto.ScoreDto(
                1L,
                180,
                "/scoreImages/1.jpg"
        );
        scoreDtos.add(getScoreDto1);
        var getScoreDto2 = new PostResponse.GetScoresDto.ScoreDto(
                2L,
                210,
                null
        );
        scoreDtos.add(getScoreDto2);

        var response = ApiUtils.success(scoreDtos);
        return ResponseEntity.ok().body(response);
    }
}
