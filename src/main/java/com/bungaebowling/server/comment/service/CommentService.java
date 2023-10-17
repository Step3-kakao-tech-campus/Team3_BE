package com.bungaebowling.server.comment.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
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

import java.util.ArrayList;
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

        var childComments = comments.stream()
                .map(comment -> commentRepository.findAllByParentId(comment.getId()))
                .toList();

        List<Comment> filteredComments = new ArrayList<>();
        List<List<Comment>> filteredChildComments = new ArrayList<>();

        for (int i = 0; i < comments.size(); i++) {
            // 삭제된 메시지가 아니거나, 자식 댓글이 빈게 아닌 경우 출력
            if (comments.get(i).getUser() != null || !childComments.get(i).isEmpty()) {
                filteredComments.add(comments.get(i));
                filteredChildComments.add(childComments.get(i));
            }
        }
        Long lastKey = comments.isEmpty() ? CursorRequest.NONE_KEY : comments.get(comments.size() - 1).getId();

        return CommentResponse.GetCommentsDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), filteredComments, filteredChildComments);
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
        var user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        var comment = requestDto.createComment(user, post);

        var savedComment = commentRepository.save(comment);
        return new CommentResponse.CreateDto(savedComment.getId());
    }

    @Transactional
    public CommentResponse.CreateDto createReply(Long userId, Long postId, Long parentId, CommentRequest.CreateDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        var parent = commentRepository.findByIdAndPostIdAndParentNull(parentId, postId).orElseThrow(() -> new CustomException(ErrorCode.REPLY_TO_COMMENT_NOT_ALLOWED));

        var comment = requestDto.createReply(user, post, parent);

        var savedComment = commentRepository.save(comment);
        return new CommentResponse.CreateDto(savedComment.getId());
    }

    @Transactional
    public void edit(Long commentId, Long userId, CommentRequest.EditDto requestDto) {
        var user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!Objects.equals(comment.getUser().getId(), user.getId())) {
            throw new CustomException(ErrorCode.COMMENT_UPDATE_FAILED);
        }

        comment.updateContent(requestDto.content());
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        var user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        var comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!Objects.equals(comment.getUser().getId(), user.getId())) {
            throw new CustomException(ErrorCode.COMMENT_DELETE_FAILED);
        }

        comment.delete();
    }
}
