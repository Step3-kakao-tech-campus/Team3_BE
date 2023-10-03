package com.bungaebowling.server.comment.service;

import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server.comment.dto.CommentRequest;
import com.bungaebowling.server.comment.repository.CommentRepository;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    final private UserRepository userRepository;

    final private PostRepository postRepository;

    final private CommentRepository commentRepository;

    @Transactional
    public Long create(Long userId, Long postId, CommentRequest.CreateDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new Exception404("존재하지 않는 유저의 접근입니다."));
        var post = postRepository.findById(postId).orElseThrow(() -> new Exception404("존재하지 않는 모집글입니다."));
        var comment = requestDto.createComment(user, post);

        return commentRepository.save(comment).getId();
    }

    @Transactional
    public Long createReply(Long userId, Long postId, Long parentId, CommentRequest.CreateDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new Exception404("존재하지 않는 유저의 접근입니다."));
        var post = postRepository.findById(postId).orElseThrow(() -> new Exception404("존재하지 않는 모집글입니다."));
        var parent = commentRepository.findById(parentId).orElseThrow(() -> new Exception404("존재하지 않는 부모 댓글입니다."));

        var comment = requestDto.createReply(user, post, parent);

        return commentRepository.save(comment).getId();
    }
}
