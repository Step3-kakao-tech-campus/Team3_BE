package com.bungaebowling.server.post.service;

import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.dto.PostRequest;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;

    public static final int DEFAULT_SIZE = 20;

    @Transactional
    public Long create(Long userId, PostRequest.CreatePostDto request) {

        User user = findUserById(userId);

        Long districtId = request.districtId();

        return savePost(user, districtId, request); // 저장로직 따로 분리

    }

    private Long savePost(User user, Long districtId, PostRequest.CreatePostDto request) { // 저장로직 따로 분리

        District district = districtRepository.findById(districtId).orElseThrow(() -> new Exception404("존재하지 않는 행정 구역입니다."));

        Post post = request.toEntity(user, district);

        return postRepository.save(post).getId();

    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public PostResponse.GetPostDto read(Long postId) {

        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        post.addViewCount(); // 조회수 1 증가

        return new PostResponse.GetPostDto(post);

    }

    public PostResponse.GetPostsDto readPosts(CursorRequest cursorRequest, Integer cityId, Integer countryId, Integer districtId, Boolean all) {

        List<Post> posts = findPosts(cursorRequest, cityId, countryId, districtId, all);

        Long lastKey = posts.isEmpty() ? CursorRequest.NONE_KEY : posts.get(posts.size() - 1).getId();

        return PostResponse.GetPostsDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), posts);

    }

    private List<Post> findPosts(CursorRequest cursorRequest, Integer cityId, Integer countryId, Integer districtId, Boolean all) {

        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;

        Pageable pageable = PageRequest.of(0, size);

        if(!cursorRequest.hasKey()) {

            if (districtId != null)
                return all ? postRepository.findAllByDistrictId(districtId) : postRepository.findAllByDistrictIdWithCloseFalse(districtId);
            if (countryId != null)
                return all ? postRepository.findAllByCountryId(countryId) : postRepository.findAllByCountryIdWithCloseFalse(countryId);
            if (cityId != null)
                return all ? postRepository.findAllByCityId(cityId) : postRepository.findAllByCityIdWithCloseFalse(cityId);

            return all ? postRepository.findAllOrderByIdDesc(pageable) : postRepository.findAllWithCloseFalse();

        } else {

            if (districtId != null)
                return all ? postRepository.findAllByDistrictId(districtId) : postRepository.findAllByDistrictIdWithCloseFalse(districtId);
            if (countryId != null)
                return all ? postRepository.findAllByCountryId(countryId) : postRepository.findAllByCountryIdWithCloseFalse(countryId);
            if (cityId != null)
                return all ? postRepository.findAllByCityId(cityId) : postRepository.findAllByCityIdWithCloseFalse(cityId);

            return all ? postRepository.findAllByIdLessThanOrderByIdDesc(cursorRequest.key(), pageable) : postRepository.findAllWithCloseFalse();

        }

    }

    @Transactional
    public void update(Long userId, Long postId, PostRequest.UpdatePostDto request) {

        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        if (!post.isMine(userId)) {
            throw new Exception403("모집글에 대한 수정 권한이 없습니다.");
        }

        post.update(
                request.title(),
                request.content(),
                request.startTime(),
                request.dueTime(),
                request.isClose()
        );

    }

    @Transactional
    public void delete(Long userId, Long postId) {

        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        if (!post.isMine(userId)) {
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
