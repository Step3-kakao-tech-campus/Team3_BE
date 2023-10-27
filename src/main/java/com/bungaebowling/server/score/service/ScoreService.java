package com.bungaebowling.server.score.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
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
    public void create(Long userId, Long postId, Integer scoreNum, MultipartFile image) {
        Post post = findPostById(postId);

        if (!post.getIsClose()) {
            throw new CustomException(ErrorCode.POST_NOT_CLOSE, "아직 점수를 등록할 수 없습니다.");
        }

        if (scoreNum == null) {
            throw new CustomException(ErrorCode.SCORE_UPLOAD_FAILED, "점수를 입력해주세요.");
        }

        if (scoreNum < 0 || scoreNum > 300) {
            throw new CustomException(ErrorCode.SCORE_UPLOAD_FAILED, "0~300 사이의 숫자만 입력해주세요.");
        }

        if (image == null) { // null 체크 - null인 경우
            saveScoreWithoutImage(userId, post, scoreNum);
        } else { // null 체크 - null이 아닌 경우
            saveScoreWithImage(userId, post, scoreNum, image);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void saveScoreWithoutImage(Long userId, Post post, Integer scoreNum) {
        User user = findUserById(userId);
        LocalDateTime createTime = LocalDateTime.now();

        Score score = Score.builder()
                .scoreNum(scoreNum)
                .resultImageUrl(null)
                .post(post)
                .user(user)
                .createdAt(createTime)
                .accessImageUrl(null)
                .build();

        scoreRepository.save(score);
    }

    private void saveScoreWithImage(Long userId, Post post, Integer scoreNum, MultipartFile image) {
        User user = findUserById(userId);
        LocalDateTime createTime = LocalDateTime.now();

        String imageUrl = awsS3Service.uploadScoreFile(user.getId(), post.getId(), "score", createTime, image);

        Score score = Score.builder()
                .scoreNum(scoreNum)
                .resultImageUrl(imageUrl)
                .post(post)
                .user(user)
                .createdAt(createTime)
                .accessImageUrl(awsS3Service.getImageAccessUrl(imageUrl))
                .build();

        scoreRepository.save(score);
    }

    @Transactional
    public void update(Long userId, Long postId, Long scoreId, Integer scoreNum, MultipartFile image) {
        Post post = findPostById(postId);

        if (!post.isMine(userId)) {
            throw new CustomException(ErrorCode.SCORE_UPDATE_PERMISSION_DENIED);
        }

        Integer scoreNumCheck = Optional.ofNullable(scoreNum)
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_UPLOAD_FAILED, "점수를 입력해주세요."));

        User user = findUserById(userId);
        Score score = findScoreById(scoreId);
        LocalDateTime updateTime = LocalDateTime.now();

        if (image == null) { // null 체크 - null인 경우
            score.updateWithoutFile(scoreNumCheck, updateTime);
        } else { // null 체크 - null이 아닌 경우
            if (score.getResultImageUrl() != null) { // 기존에 파일이 있다면
                awsS3Service.deleteFile(score.getResultImageUrl()); // 기존에 있던 파일 지워주기
            }
            String imageurl = awsS3Service.uploadScoreFile(user.getId(), postId, "score", updateTime, image);
            String accessImageUrl = awsS3Service.getImageAccessUrl(imageurl);

            score.updateWithFile(scoreNumCheck, imageurl, updateTime, accessImageUrl);
        }
    }

    private Score findScoreById(Long scoreId) {
        return scoreRepository.findById(scoreId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCORE_NOT_FOUND));
    }

    @Transactional
    public void delete(Long userId, Long postId, Long scoreId) {
        Post post = findPostById(postId);

        if (!post.isMine(userId)) {
            throw new CustomException(ErrorCode.SCORE_DELETE_PERMISSION_DENIED);
        }

        Score score = findScoreById(scoreId);

        deleteScore(score);
    }

    private void deleteScore(Score score) {
        awsS3Service.deleteFile(score.getResultImageUrl());
        scoreRepository.delete(score);
    }

    public int calculateAverage(List<Score> scores) {
        return (int) scores.stream()
                .mapToInt(Score::getScoreNum)
                .average()
                .orElse(0.0);
    }

    public int findMaxScore(List<Score> scores) {
        return scores.stream()
                .mapToInt(Score::getScoreNum)
                .max()
                .orElse(0);
    }

    public int findMinScore(List<Score> scores) {
        return scores.stream()
                .mapToInt(Score::getScoreNum)
                .min()
                .orElse(0);
    }
}
