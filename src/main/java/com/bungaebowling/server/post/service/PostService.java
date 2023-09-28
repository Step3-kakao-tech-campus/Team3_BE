package com.bungaebowling.server.post.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


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
    public PostResponse.GetPostDto read(Long postId) {

        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        post.addViewCount(); // 조회수 1 증가

        PostResponse.GetPostDto.PostDto postDto = convertPostToPostDto(post); // post to postDto

        return new PostResponse.GetPostDto(postDto);

    }

    private PostResponse.GetPostDto.PostDto convertPostToPostDto(Post post) { // dto 변환 함수
        return new PostResponse.GetPostDto.PostDto(
                post.getId(),
                post.getTitle(),
                post.getUserName(),
                post.getProfilePath(),
                post.getDistrictName(),
                post.getCurrentNumber(),
                post.getContent(),
                post.getStartTime(),
                post.getDueTime(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getEditedAt(),
                post.getIsClose()
        );
    }

    public PostResponse.GetPostsDto readPosts(Integer cityId, Integer countryId, Integer districtId, Boolean all) {

        CursorRequest cursorRequest = new CursorRequest(1L, 20);
        List<PostResponse.GetPostsDto.PostDto> postDtos = new ArrayList<>();

        List<Post> posts = findPosts(cityId, countryId, districtId, all);

        for(Post post : posts) {
            postDtos.add(convertPostToPostDtos(post));
        }

        return new PostResponse.GetPostsDto(cursorRequest, postDtos);

    }

    private List<Post> findPosts(Integer cityId, Integer countryId, Integer districtId, Boolean all) {

        List<Post> posts;

        if (districtId == null) {
            if (countryId == null){
                if(cityId == null) {
                    posts = isAll(all, null);
                } else {
                    posts = isAll(all, cityId);
                }
            } else {
                posts = isAll(all, countryId);
            }
        } else {
            posts = isAll(all, districtId);
        }

        return posts;

    }

    private List<Post> isAll(Boolean all, Integer id) { // 전체 조회인지, 모집 중 조회인지

        List<Post> posts;

        if (all) {
            if(id == null) {
                posts = postRepository.findAll();
                return posts;
            }
            posts = postRepository.findAllById(id);
        } else {
            if(id == null) {
                posts = postRepository.findAllWithCloseFalse();
                return posts;
            }
            posts = postRepository.findAllByIdWithCloseFalse(id);
        }

        return posts;

    }

    private PostResponse.GetPostsDto.PostDto convertPostToPostDtos (Post post) { // dto 변환 함수
        return new PostResponse.GetPostsDto.PostDto(
                post.getId(),
                post.getTitle(),
                post.getDueTime(),
                post.getDistrictName(),
                post.getStartTime(),
                post.getUserName(),
                post.getProfilePath(),
                post.getCurrentNumber(),
                post.getIsClose()
        );
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
