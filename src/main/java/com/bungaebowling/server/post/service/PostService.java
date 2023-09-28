package com.bungaebowling.server.post.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Long create(Post post) {
        return savePost(post); // 저장로직 따로 분리
    }

    private Long savePost(Post post) { // 저장로직 따로 분리
        return postRepository.save(post).getId();
    }

    @Transactional
    public void update (User user, Long postId, Post newPost) {
        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        if (!post.isMine(user)) {
            throw new Exception403("모집글에 대한 수정 권한이 없습니다.");
        }

        post.update(newPost);
    }

    @Transactional
    public void delete (User user, Long postId) {
        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        if (!post.isMine(user)) {
            throw new Exception403("모집글에 대한 삭제 권한이 없습니다.");
        }

        deletePost(post);
    }

    private void deletePost(Post post) { // 삭제 로직 따로 분리
        postRepository.delete(post);
    }

    private Post findById(Long postId) { // id로 post 찾는 로직 따로 분리
        return postRepository.findById(postId)
                .orElseThrow(() -> new Exception404("모집글을 찾을 수 없습니다."));
    }

}
