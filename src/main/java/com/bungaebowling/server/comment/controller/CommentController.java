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
    public ResponseEntity<?> getComments() {
        CursorRequest cursorRequest = new CursorRequest(1L, 20);
        List<CommentResponse.GetCommentsDto.CommentDto> commentsDtos = new ArrayList<>();
        var commentDto1 = new CommentResponse.GetCommentsDto.CommentDto(
                1L,
                1L,
                "볼링조아",
                null,
                "저 참여하고 싶습니다.",
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(
                        new CommentResponse.GetCommentsDto.CommentDto.ChildCommentDto(
                                2L,
                                2L,
                                "김볼링",
                                null,
                                "몇명이신가요?",
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        ),
                        new CommentResponse.GetCommentsDto.CommentDto.ChildCommentDto(
                                3L,
                                1L,
                                "볼링조아",
                                null,
                                "2인 참여 가능할까요?",
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        ),
                        new CommentResponse.GetCommentsDto.CommentDto.ChildCommentDto(
                                4L,
                                3L,
                                "거터처리반",
                                null,
                                "동반 1인입니다!",
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        )
                )
        );
        commentsDtos.add(commentDto1);
        var commentDto2 = new CommentResponse.GetCommentsDto.CommentDto(
                4L,
                3L,
                "거터처리반",
                null,
                "동반 1인입니다!",
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );
        commentsDtos.add(commentDto2);

        var getCommentsDto = new CommentResponse.GetCommentsDto(cursorRequest, commentsDtos);

        var response = ApiUtils.success(getCommentsDto);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long postId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody @Valid CommentRequest.CreateDto requestDto,
                                    Errors errors) throws URISyntaxException {

        var savedId = commentService.create(userDetails.getId(), postId, requestDto);
        return ResponseEntity.created(new URI("/api/posts/" + postId + "/comments/" + savedId))
                .body(ApiUtils.success(201));
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<?> createReply(@PathVariable Long postId,
                                         @PathVariable Long commentId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestBody @Valid CommentRequest.CreateDto requestDto,
                                         Errors errors) throws URISyntaxException {

        var savedId = commentService.createReply(userDetails.getId(), postId, commentId, requestDto);
        return ResponseEntity.created(new URI("/api/posts/" + postId + "/comments/" + savedId))
                .body(ApiUtils.success(201));
    }
}
