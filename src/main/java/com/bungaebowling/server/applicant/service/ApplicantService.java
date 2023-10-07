package com.bungaebowling.server.applicant.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception403;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.applicant.dto.ApplicantRequest;
import com.bungaebowling.server.applicant.dto.ApplicantResponse;
import com.bungaebowling.server.applicant.repository.ApplicantRepository;
import com.bungaebowling.server.post.Post;
import com.bungaebowling.server.post.repository.PostRepository;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.rate.UserRate;
import com.bungaebowling.server.user.rate.repository.UserRateRepository;
import com.bungaebowling.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ApplicantService {

    public static final int DEFAULT_SIZE = 20;

    private final ApplicantRepository applicantRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    private final UserRateRepository userRateRepository;

    public ApplicantResponse.GetApplicantsDto getApplicants(Long userId, Long postId, CursorRequest cursorRequest){
        Post post = getPost(postId);

        if (!Objects.equals(post.getUser().getId(), userId))
            throw new Exception403("자신의 모집글이 아닙니다.");

        Long participantNumber = applicantRepository.countByPostId(post.getId());
        Long currentNumber = applicantRepository.countByPostIdAndIsStatusTrue(post.getId());
        List<Applicant> applicants = loadApplicants(cursorRequest, post.getId());
        Long lastKey = applicants.isEmpty() ? CursorRequest.NONE_KEY : applicants.get(applicants.size() - 1).getId();

        var ratings = applicants.stream().map(applicant -> {
                    return userRateRepository.findAllByUserId(applicant.getUser().getId()).stream()
                            .mapToInt(UserRate::getStarCount)
                            .average().orElse(0.0);
                }).toList();

        return ApplicantResponse.GetApplicantsDto.of(
                cursorRequest.next(lastKey, DEFAULT_SIZE),
                participantNumber,
                currentNumber,
                applicants,
                ratings);
    }

    private List<Applicant> loadApplicants(CursorRequest cursorRequest, Long postId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);

        if(!cursorRequest.hasKey()){
            return applicantRepository.findAllByPostIdOrderByIdDesc(postId, pageable);
        }else{
            return applicantRepository.findAllByPostIdLessThanOrderByIdDesc(cursorRequest.key(), postId, pageable);
        }
    }

    @Transactional
    public void create(Long userId, Long postId){
        User user = getUser(userId);
        Post post = getPost(postId);

        if (Objects.equals(post.getUser().getId(), user.getId()))
            throw new Exception400("본인의 모집글에 신청할 수 없습니다.");

        applicantRepository.findByUserIdAndPostId(userId, postId).ifPresent(applicant -> {
            throw new Exception400("이미 신청된 사용자입니다.");
        });

        if(post.getIsClose() || LocalDateTime.now().isAfter(post.getDueTime())){
            throw new Exception400("이미 마감된 모집글입니다.");
        }

        Applicant applicant = Applicant.builder().user(user).post(post).build();
        applicantRepository.save(applicant);
    }

    @Transactional
    public void accept(Long userId, Long applicantId, ApplicantRequest.UpdateDto requestDto){
        Applicant applicant = getApplicantWithPost(applicantId);

        if (!(Objects.equals(userId, applicant.getPost().getUser().getId())))
            throw new Exception403("작성자만 수락 가능합니다.");

        applicant.updateStatus(requestDto.status());
    }

    @Transactional
    public void reject(Long userId, Long applicantId){
        Applicant applicant = getApplicantWithPost(applicantId);
        var isPostOwner = Objects.equals(userId, applicant.getPost().getUser().getId());
        var isApplicantOwner = Objects.equals(userId, applicant.getUser().getId());

        if (!(isApplicantOwner || isPostOwner))
            throw new Exception403("권한이 없습니다.");

        applicantRepository.delete(applicant);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new Exception404("존재하지 않는 사용자입니다.")
        );
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new Exception404("존재하지 않는 모집글입니다.")
        );
    }

    private Applicant getApplicantWithPost(Long applicantId) {
        return applicantRepository.findByIdJoinFetchPost(applicantId).orElseThrow(
                () -> new Exception404("존재하지 않는 신청입니다.")
        );
    }
}
