package com.bungaebowling.server.comment.service;


import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.comment.Comment;
import com.bungaebowling.server.comment.dto.CommentRequest;
import com.bungaebowling.server.comment.dto.CommentResponse;
import com.bungaebowling.server.comment.repository.CommentRepository;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

    final private UserRepository userRepository;

    final private PostRepository postRepository;

    final private CommentRepository commentRepository;

    public static final int DEFAULT_SIZE = 20;

    public CommentResponse.GetCommentsDto getComments(CursorRequest cursorRequest, Long postId) {
        List<Comment> comments = findComments(cursorRequest, postId);

        Long lastKey = comments.isEmpty() ? CursorRequest.NONE_KEY : comments.get(comments.size() - 1).getId();

        var childComments = comments.stream()
                .map(comment -> commentRepository.findAllByParentId(comment.getId()))
                .toList();

        return CommentResponse.GetCommentsDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), comments, childComments);
    }

    private List<Comment> findComments(CursorRequest cursorRequest, Long postId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;

        Pageable pageable = PageRequest.of(0, size);

        if (!cursorRequest.hasKey()) {
            return commentRepository.findAllByPostIdAndIsParentNullOrderById(postId, pageable);
        }
        return commentRepository.findAllByPostIdAndIsParentNullAndIdGreaterThanOrderById(postId, cursorRequest.key(), pageable);
    }

    @Transactional
    public CommentResponse.CreateDto create(Long userId, Long postId, CommentRequest.CreateDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new Exception404("존재하지 않는 유저의 접근입니다."));
        var post = postRepository.findById(postId).orElseThrow(() -> new Exception404("존재하지 않는 모집글입니다."));
        var comment = requestDto.createComment(user, post);

        var savedComment = commentRepository.save(comment);
        return new CommentResponse.CreateDto(savedComment.getId());
    }

    @Transactional
    public CommentResponse.CreateDto createReply(Long userId, Long postId, Long parentId, CommentRequest.CreateDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new Exception404("존재하지 않는 유저의 접근입니다."));
        var post = postRepository.findById(postId).orElseThrow(() -> new Exception404("존재하지 않는 모집글입니다."));
        var parent = commentRepository.findByIdAndPostIdAndParentNull(parentId, postId).orElseThrow(() -> new Exception400("대댓글을 작성할 수 없는 댓글입니다."));

        var comment = requestDto.createReply(user, post, parent);

        var savedComment = commentRepository.save(comment);
        return new CommentResponse.CreateDto(savedComment.getId());
    }

    @Transactional
    public void edit(Long commentId, Long userId, CommentRequest.EditDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new Exception404("존재하지 않는 유저의 접근입니다."));
        var comment = commentRepository.findById(commentId).orElseThrow(() -> new Exception404("존재하지 않는 댓글입니다."));

        if (!Objects.equals(comment.getUser().getId(), user.getId())) {
            throw new Exception403("본인의 댓글만 수정 가능합니다.");
        }

        comment.updateContent(requestDto.content());
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new Exception404("존재하지 않는 유저의 접근입니다."));
        var comment = commentRepository.findById(commentId).orElseThrow(() -> new Exception404("존재하지 않는 댓글입니다."));

        if (!Objects.equals(comment.getUser().getId(), user.getId())) {
            throw new Exception403("본인의 댓글만 삭제 가능합니다.");
        }

        comment.delete();
    }
}
