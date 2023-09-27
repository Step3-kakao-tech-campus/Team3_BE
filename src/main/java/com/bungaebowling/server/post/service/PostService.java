package com.bungaebowling.server.post.service;

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

}
