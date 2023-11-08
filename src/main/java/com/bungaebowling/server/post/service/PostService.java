package com.bungaebowling.server.post.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.applicant.repository.ApplicantRepository;
import com.bungaebowling.server.city.country.district.District;
import com.bungaebowling.server.city.country.district.repository.DistrictRepository;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.dto.PostRequest;
import com.bungaebowling.server.post.dto.PostResponse;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.score.repository.ScoreRepository;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.rate.UserRate;
import com.bungaebowling.server.user.rate.repository.UserRateRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bungaebowling.server.post.service.PostSpecification.*;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;
    private final ScoreRepository scoreRepository;
    private final ApplicantRepository applicantRepository;
    private final UserRateRepository userRateRepository;

    public static final int DEFAULT_SIZE = 20;

    @Transactional
    public PostResponse.CreateDto createPostWithApplicant(Long userId, PostRequest.CreatePostDto request) {

        User user = findUserById(userId);

        Long districtId = request.districtId();

        var savedPost = savePost(user, districtId, request);

        saveApplicant(savedPost, user);

        return new PostResponse.CreateDto(savedPost.getId());
    }

    private Post savePost(User user, Long districtId, PostRequest.CreatePostDto request) {

        District district = districtRepository.findById(districtId).orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));

        Post post = request.toEntity(user, district);

        return postRepository.save(post);
    }

    private void saveApplicant(Post post, User user) {
        applicantRepository.save(
                Applicant.builder()
                        .post(post)
                        .user(user)
                        .status(true)
                        .build()
        );
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public PostResponse.GetPostDto read(Long postId) {

        Post post = postRepository.findByIdJoinFetch(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND)); // post 찾는 코드 빼서 함수화

        post.addViewCount(); // 조회수 1 증가

        Long currentNumber = (long) applicantRepository.findAllByPostIdAndStatusTrueOrderByUserIdDesc(post.getId()).size();

        return new PostResponse.GetPostDto(post, currentNumber);

    }

    public PostResponse.GetPostsDto readPosts(CursorRequest cursorRequest, Long cityId, Long countryId, Long districtId, Boolean all) {

        List<Post> posts = findPosts(cursorRequest, cityId, countryId, districtId, all);

        Long lastKey = getLastKey(posts);

        Map<Long, List<Applicant>> applicantMap = getApplicantMap(posts);
        Map<Long, Long> currentNumberMap = getCurrentNumberMap(posts, applicantMap);

        return PostResponse.GetPostsDto.of(cursorRequest.next(lastKey, DEFAULT_SIZE), posts, currentNumberMap);
    }

    private List<Post> findPosts(CursorRequest cursorRequest, Long cityId, Long countryId, Long districtId, Boolean all) {

        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;

        Pageable pageable = PageRequest.of(0, size);

        if (!cursorRequest.hasKey()) {

            if (districtId != null)
                return all ? postRepository.findAllByDistrictIdOrderByIdDesc(districtId, pageable) : postRepository.findAllByDistrictIdWithCloseFalseOrderByIdDesc(districtId, pageable);
            if (countryId != null)
                return all ? postRepository.findAllByCountryIdOrderByIdDesc(countryId, pageable) : postRepository.findAllByCountryIdWithCloseFalseOrderByIdDesc(countryId, pageable);
            if (cityId != null)
                return all ? postRepository.findAllByCityIdOrderByIdDesc(cityId, pageable) : postRepository.findAllByCityIdWithCloseFalseOrderByIdDesc(cityId, pageable);

            return all ? postRepository.findAllOrderByIdDesc(pageable) : postRepository.findAllWithCloseFalseOrderByIdDesc(pageable);
        }
        if (districtId != null)
            return all ? postRepository.findAllByDistrictIdAndIdLessThanOrderByIdDesc(districtId, cursorRequest.key(), pageable) : postRepository.findAllByDistrictIdAndIdLessThanWithCloseFalseOrderByIdDesc(districtId, cursorRequest.key(), pageable);
        if (countryId != null)
            return all ? postRepository.findAllByCountryIdAndIdLessThanOrderByIdDesc(countryId, cursorRequest.key(), pageable) : postRepository.findAllByCountryIdAndIdLessThanWithCloseFalseOrderByIdDesc(countryId, cursorRequest.key(), pageable);
        if (cityId != null)
            return all ? postRepository.findAllByCityIdAndIdLessThanOrderByIdDesc(cityId, cursorRequest.key(), pageable) : postRepository.findAllByCityIdAndIdLessThanWithCloseFalseOrderByIdDesc(cityId, cursorRequest.key(), pageable);

        return all ? postRepository.findAllByIdLessThanOrderByIdDesc(cursorRequest.key(), pageable) : postRepository.findAllByIdLessThanWithCloseFalseOrderByIdDesc(cursorRequest.key(), pageable);
    }

    @Transactional
    public void update(Long userId, Long postId, PostRequest.UpdatePostDto request) {

        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        if (!post.isMine(userId)) {
            throw new CustomException(ErrorCode.POST_UPDATE_PERMISSION_DENIED);
        }

        LocalDateTime editedAt = LocalDateTime.now();

        post.update(
                request.title(),
                request.content(),
                request.startTime(),
                request.dueTime(),
                editedAt
        );
    }

    @Transactional
    public void delete(Long userId, Long postId) {

        Post post = findById(postId); // post 찾는 코드 빼서 함수화

        if (!post.isMine(userId)) {
            throw new CustomException(ErrorCode.POST_DELETE_PERMISSION_DENIED);
        }

        deletePost(post);
    }

    @Transactional
    public void updateIsClose(Long userId, Long postId, PostRequest.UpdatePostIsCloseDto request) {
        Post post = findById(postId);

        if (!post.isMine(userId)) {
            throw new CustomException(ErrorCode.POST_CLOSE_PERMISSION_DENIED);
        }

        post.updateIsClose(request.isClose());
    }

    public PostResponse.GetParticipationRecordsDto getParticipationRecords(CursorRequest cursorRequest, Long userId,
                                                                           String condition, String status, Long cityId, String start, String end) {
        List<Post> posts = loadPosts(cursorRequest, userId, condition, status, cityId, start, end);

        Map<Long, List<Score>> scoreMap = getScoreMap(userId, posts);
        Map<Long, List<Applicant>> applicantMap = getApplicantMap(posts);
        Map<Long, List<User>> memberMap = getMemberMap(posts, applicantMap);
        Map<Long, List<UserRate>> rateMap = getRateMap(userId, posts, applicantMap);
        Map<Long, Long> applicantIdMap = getApplicantIdMap(userId, posts, applicantMap);
        Map<Long, Long> currentNumberMap = getCurrentNumberMap(posts, applicantMap);

        Long lastKey = getLastKey(posts);
        return PostResponse.GetParticipationRecordsDto.of(
                cursorRequest.next(lastKey, DEFAULT_SIZE),
                posts,
                scoreMap,
                memberMap,
                rateMap,
                applicantIdMap,
                currentNumberMap
        );
    }

    private List<Post> loadPosts(CursorRequest cursorRequest, Long userId, String condition, String status, Long cityId, String start, String end) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Order.desc("id")));

        Specification<Post> spec = Specification.where(conditionEqual(condition, userId))
                .and(statusEqual(status))
                .and(cityIdEqual(cityId))
                .and(createdAtBetween(start, end));

        if (cursorRequest.hasKey()) {
            spec = spec.and(postIdLessThan(cursorRequest.key()));
        }
        return postRepository.findAll(spec, pageable).getContent();
    }

    private Map<Long, List<Score>> getScoreMap(Long userId, List<Post> posts) {
        return posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> scoreRepository.findAllByUserIdAndPostIdOrderById(userId, post.getId())
        ));
    }

    private Map<Long, List<Applicant>> getApplicantMap(List<Post> posts) {
        return posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> applicantRepository.findAllByPostIdAndStatusTrueOrderByUserIdDesc(post.getId())
        ));
    }

    private Map<Long, List<User>> getMemberMap(List<Post> posts, Map<Long, List<Applicant>> applicantMap) {
        return posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> applicantMap.get(post.getId()).stream()
                        .map(Applicant::getUser)
                        .toList()
        ));
    }

    private Map<Long, List<UserRate>> getRateMap(Long userId, List<Post> posts, Map<Long, List<Applicant>> applicantMap) {
        return posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> {
                    List<UserRate> userRates = new ArrayList<>();
                    applicantMap.get(post.getId()).stream()
                            .filter(applicant -> applicant.getUser().getId().equals(userId))
                            .forEach(applicant ->
                                    userRates.addAll(userRateRepository.findAllByApplicantId(applicant.getId()))
                            );
                    return userRates;
                }
        ));
    }

    private Map<Long, Long> getApplicantIdMap(Long userId, List<Post> posts, Map<Long, List<Applicant>> applicantMap) {
        return posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> applicantMap.get(post.getId()).stream()
                        .filter(applicant -> userId.equals(applicant.getUser().getId()))
                        .map(Applicant::getId)
                        .findFirst()
                        .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND))
        ));
    }

    private Map<Long, Long> getCurrentNumberMap(List<Post> posts, Map<Long, List<Applicant>> applicantMap) {
        return posts.stream().collect(Collectors.toMap(
                Post::getId,
                post -> (long) applicantMap.get(post.getId()).size()
        ));
    }

    private void deletePost(Post post) { // 삭제 로직 따로 분리
        postRepository.delete(post);
    }

    private Post findById(Long postId) { // id로 post 찾는 로직 따로 분리
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private static Long getLastKey(List<Post> posts) {
        return posts.isEmpty() ? CursorRequest.NONE_KEY : posts.get(posts.size() - 1).getId();
    }
}
