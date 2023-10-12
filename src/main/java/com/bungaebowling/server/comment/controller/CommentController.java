package com.bungaebowling.server.comment.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.comment.dto.CommentRequest;
import com.bungaebowling.server.comment.dto.CommentResponse;
import com.bungaebowling.server.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    final private CommentService commentService;

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long postId, CursorRequest cursorRequest) {
        var responseDto = commentService.getComments(cursorRequest, postId);

        return ResponseEntity.ok().body(ApiUtils.success(responseDto));
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long postId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody @Valid CommentRequest.CreateDto requestDto,
                                    Errors errors) throws URISyntaxException {

        var responseDto = commentService.create(userDetails.getId(), postId, requestDto);
        return ResponseEntity.ok().body(ApiUtils.success(responseDto));
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<?> createReply(@PathVariable Long postId,
                                         @PathVariable Long commentId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestBody @Valid CommentRequest.CreateDto requestDto,
                                         Errors errors) throws URISyntaxException {

        var responseDto = commentService.createReply(userDetails.getId(), postId, commentId, requestDto);
        return ResponseEntity.ok().body(ApiUtils.success(responseDto));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> edit(@PathVariable Long postId,
                                  @PathVariable Long commentId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestBody @Valid CommentRequest.EditDto requestDto,
                                  Errors errors) {
        commentService.edit(commentId, userDetails.getId(), requestDto);

        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long postId,
                                    @PathVariable Long commentId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.delete(commentId, userDetails.getId());

        return ResponseEntity.ok().body(ApiUtils.success());
    }
}
