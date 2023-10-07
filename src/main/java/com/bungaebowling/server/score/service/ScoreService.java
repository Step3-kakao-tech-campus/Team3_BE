package com.bungaebowling.server.score.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.utils.AwsS3Service;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.score.Score;
import com.bungaebowling.server.score.dto.ScoreResponse;
import com.bungaebowling.server.score.repository.ScoreRepository;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ScoreService {

    private final AwsS3Service awsS3Service;

    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public ScoreResponse.GetScoresDto readScores(Long postId) {
        List<Score> scores = findScores(postId);
        return ScoreResponse.GetScoresDto.of(scores);
    }

    private List<Score> findScores(Long postId) {
        return scoreRepository.findAllByPostId(postId);
    }

    @Transactional
    public Long create(Long userId, Long postId, List<Integer> scoreNums, List<MultipartFile> images) {
        Post post = findPostById(postId);

        if(!post.getIsClose()) {
            throw new Exception400("아직 점수를 등록할 수 없습니다.");
        }

        if(CollectionUtils.isEmpty(scoreNums)) { // null 체크도 해즘
            throw new Exception400("점수를 입력해주세요.");
        }

        if(CollectionUtils.isEmpty(images)) { // null 체크도 해즘
            throw new Exception400("점수 사진을 등록해주세요.");
        }

        return saveScores(userId, post, scoreNums, images);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new Exception404("모집글을 찾을 수 없습니다."));
    }

    public Long saveScores(Long userId, Post post, List<Integer> scoreNums, List<MultipartFile> images) {
        User user = findUserById(userId);
        LocalDateTime createTime = LocalDateTime.now();
        List<String> imageURls = awsS3Service.uploadMultiFile(user.getName(), post.getId(),"score", createTime,images);

        if(!CollectionUtils.isEmpty(imageURls)) {
            for (int i = 0; i < imageURls.size(); i++) {

                Score score = Score.builder()
                        .scoreNum(scoreNums.get(i))
                        .resultImageUrl(imageURls.get(i))
                        .post(post)
                        .user(user)
                        .createdAt(createTime)
                        .build();

                scoreRepository.save(score);
            }
        }

        return post.getId(); // 점수가 저장된 postId를 반환
    }

    @Transactional
    public void update(Long userId, Long postId, Long scoreId, Integer scoreNum, MultipartFile image) {
        Post post = findPostById(postId);

        if(!post.isMine(userId)) {
            throw new Exception403("점수 정보에 대한 수정 권한이 없습니다.");
        }

        User user = findUserById(userId);
        Score score = findScoreById(scoreId);
        LocalDateTime updateTime = LocalDateTime.now();

        Integer scoreNumCheck = Optional.ofNullable(scoreNum)
                .orElseThrow(() -> new Exception400("점수를 입력해주세요."));
        MultipartFile imageCheck = Optional.ofNullable(image)
                .orElseThrow(() -> new Exception400("점수 사진을 등록해주세요."));

        String imageurl = awsS3Service.uploadScoreFile(user.getName(), postId,"score", updateTime,imageCheck);

        score.update(user, post, scoreNumCheck, imageurl, updateTime);
    }

    private Score findScoreById(Long scoreId) {
        return scoreRepository.findById(scoreId)
                .orElseThrow(() -> new Exception404("점수 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public void delete(Long userId, Long postId, Long scoreId) {
        Post post = findPostById(postId);

        if(!post.isMine(userId)) {
            throw new Exception403("점수 정보에 대한 삭제 권한이 없습니다.");
        }

        Score score = findScoreById(scoreId);

        deleteScore(score);
    }

    private void deleteScore(Score score) {
        awsS3Service.deleteFile(score.getResultImageUrl());
        scoreRepository.delete(score);
    }

}
