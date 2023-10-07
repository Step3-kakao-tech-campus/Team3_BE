package com.bungaebowling.server.score.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
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
        return saveScores(userId, postId, scoreNums, images);
    }



    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다."));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new Exception404("모집글을 찾을 수 없습니다."));
    }

    public Long saveScores(Long userId, Long postId, List<Integer> scoreNums, List<MultipartFile> images) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        LocalDateTime createTime = LocalDateTime.now();
        List<String> imageURls = awsS3Service.uploadMultiFile(user.getName(), postId,"score", createTime,images);

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

        return postId; // 점수가 저장된 postId를 반환
    }

    @Transactional
    public void update(Long userId, Long postId, Long scoreId, Integer scoreNum, MultipartFile image) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        Score score = findScoreById(scoreId);

        Integer scoreNumCheck = Optional.ofNullable(scoreNum)
                .orElseThrow(() -> new Exception400("점수를 입력해주세요."));

        MultipartFile imageCheck = Optional.ofNullable(image)
                .orElseThrow(() -> new Exception400("점수 사진을 등록해주세요."));

        LocalDateTime updateTime = LocalDateTime.now();
        String imageurl = awsS3Service.uploadScoreFile(user.getName(), postId,"score", updateTime,imageCheck);

        score.update(user, post, scoreNumCheck, imageurl, updateTime);
    }

    private Score findScoreById(Long scoreId) {
        return scoreRepository.findById(scoreId)
                .orElseThrow(() -> new Exception404("점수 정보를 찾을 수 없습니다."));
    }
}