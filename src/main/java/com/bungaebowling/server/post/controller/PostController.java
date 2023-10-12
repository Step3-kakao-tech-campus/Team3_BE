package com.bungaebowling.server.post.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.dto.PostRequest;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<?> getPosts(
            CursorRequest cursorRequest,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "countryId", required = false) Long countryId,
            @RequestParam(value = "districtId", required = false) Long districtId,
            @RequestParam(value = "all", defaultValue = "true") Boolean all
    ) {
        PostResponse.GetPostsDto response = postService.readPosts(cursorRequest,cityId, countryId, districtId, all);

        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(
            @PathVariable Long postId
    ) {
        PostResponse.GetPostDto response = postService.read(postId);

        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @GetMapping("/users/{userId}/participation-records")
    public ResponseEntity<?> getUserParticipationRecords(@PathVariable Long userId) {
        CursorRequest cursorRequest = new CursorRequest(1L, 20);
        List<PostResponse.GetParticipationRecordsDto.PostDto> postDtos = new ArrayList<>();
        var postDto1 = new PostResponse.GetParticipationRecordsDto.PostDto(
                1L,
                "오늘 7시에 부산대 락볼링장에서 게임하실분~~",
                LocalDateTime.now(),
                "부산광역시 금정구 장전2동",
                LocalDateTime.now(),
                2,
                false,
                List.of(),
                List.of(
                        new PostResponse.GetParticipationRecordsDto.PostDto.MemberDto(
                                2L,
                                "최볼링",
                                "/images/2.jpg",
                                true
                        ),
                        new PostResponse.GetParticipationRecordsDto.PostDto.MemberDto(
                                3L,
                                "이볼링",
                                "/images/3.jpg",
                                false
                        )
                )
        );
        postDtos.add(postDto1);

        var postDto2 = new PostResponse.GetParticipationRecordsDto.PostDto(
                1L,
                "오늘 당장 나올 사람!",
                LocalDateTime.now(),
                "부산광역시 금정구 장전2동",
                LocalDateTime.now(),
                2,
                false,
                List.of(
                        new PostResponse.GetParticipationRecordsDto.PostDto.ScoreDto(
                                1L,
                                180,
                                "/score-images/1.jpg"
                        ),
                        new PostResponse.GetParticipationRecordsDto.PostDto.ScoreDto(
                                2L,
                                210,
                                null
                        )
                ),
                List.of(
                        new PostResponse.GetParticipationRecordsDto.PostDto.MemberDto(
                                2L,
                                "최볼링",
                                "/images/2.jpg",
                                true
                        ),
                        new PostResponse.GetParticipationRecordsDto.PostDto.MemberDto(
                                3L,
                                "이볼링",
                                "/images/3.jpg",
                                false
                        )
                )
        );
        postDtos.add(postDto2);

        var getPostsDto = new PostResponse.GetParticipationRecordsDto(cursorRequest, postDtos);

        var response = ApiUtils.success(getPostsDto);

        return ResponseEntity.ok().body(response);
    }

    // ToDo : 모집글 등록 response에 모집글 id 반환하기
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}) // json 타입만 처리 가능
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostRequest.CreatePostDto request,
            Errors errors
    ) {
        PostResponse.GetPostPostDto response = postService.create(userDetails.getId(), request);

        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody @Valid PostRequest.UpdatePostDto request,
            Errors errors
    ) {
        postService.update(userDetails.getId(), postId, request);

        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        postService.delete(userDetails.getId(), postId);

        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> patchPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostRequest.UpdatePostIsCloseDto request,
            Errors errors
    ) {
        postService.updateIsClose(userDetails.getId(), postId, request);

        return ResponseEntity.ok().body(ApiUtils.success());
    }

}
