package com.bungaebowling.server.post.controller;

import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.post.dto.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    @GetMapping
    public ResponseEntity<?> getAll() {
        CursorRequest cursorRequest = new CursorRequest(1L, 20);
        List<PostResponse.GetPostsDto.PostDto> postDtos = new ArrayList<>();
        PostResponse.GetPostsDto.PostDto postDto1 = new PostResponse.GetPostsDto.PostDto(
                1L,
                "오늘 7시에 부산대 락볼링장에서 게임하실분~~",
                "9월 7일 (목) 오후 9:00",
                "부산광역시 금정구 장전2동",
                "9월 9일 (토) 오전 9:00",
                0,
                "김볼링",
                null,
                false
        );
        postDtos.add(postDto1);
        PostResponse.GetPostsDto.PostDto postDto2 = new PostResponse.GetPostsDto.PostDto(
                2L,
                "오늘 당장 나올사람!",
                "9월 7일 (목) 오후 8:00",
                "부산광역시 동래구",
                "9월 7일 (목) 오후 8:30",
                0,
                "최볼링",
                null,
                false
        );
        postDtos.add(postDto2);
        PostResponse.GetPostsDto.PostDto postDto3 = new PostResponse.GetPostsDto.PostDto(
                3L,
                "오늘 7시에 부산대 락볼링장에서 게임하실분~~",
                "9월 7일 (목) 오후 3:00",
                "부산광역시 부산진구 부전동",
                "9월 8일 (금) 오후 7:00",
                0,
                "이볼링",
                null,
                false
        );
        postDtos.add(postDto3);

        PostResponse.GetPostsDto getPostsDto = new PostResponse.GetPostsDto(cursorRequest, postDtos);

        ApiUtils.Response<?> response = ApiUtils.success(getPostsDto);
        return ResponseEntity.ok().body(response);
    }
}
