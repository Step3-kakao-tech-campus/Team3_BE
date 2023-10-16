package com.bungaebowling.server.post.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.post.dto.PostRequest;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

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
        PostResponse.GetPostsDto response = postService.readPosts(cursorRequest, cityId, countryId, districtId, all);

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
    public ResponseEntity<?> getUserParticipationRecords(
            CursorRequest cursorRequest, @PathVariable Long userId,
            @RequestParam(value = "condition", required = false, defaultValue = "all") String condition,
            @RequestParam(value = "status", required = false, defaultValue = "all") String status,
            @RequestParam(value = "cityId", required = false) Long cityId,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end) {
        var response = postService.getParticipationRecords(cursorRequest, userId, condition, status, cityId, start, end);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    // ToDo : 모집글 등록 response에 모집글 id 반환하기
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}) // json 타입만 처리 가능
    public ResponseEntity<?> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PostRequest.CreatePostDto request,
            Errors errors
    ) {
        PostResponse.GetPostPostDto response = postService.createPostWithApplicant(userDetails.getId(), request);

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
