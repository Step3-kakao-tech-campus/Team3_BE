package com.bungaebowling.server.applicant.service;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
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

    public ApplicantResponse.GetApplicantsDto getApplicants(Long userId, Long postId, CursorRequest cursorRequest) {
        Post post = getPost(postId);

        if (!isMatchedUser(userId, post))
            throw new CustomException(ErrorCode.APPLICANT_PERMISSION_DENIED, "자신의 모집글이 아닙니다.");

        Long applicantNumber = applicantRepository.countByPostIdAndUserIdNot(post.getId(), userId);
        List<Applicant> applicants = loadApplicants(cursorRequest, post.getId(), userId);
        Long lastKey = getLastKey(applicants);

        var ratings = applicants.stream().map(applicant ->
                userRateRepository.findAllByUserId(applicant.getUser().getId()).stream()
                        .mapToInt(UserRate::getStarCount)
                        .average().orElse(0.0)
        ).toList();

        return ApplicantResponse.GetApplicantsDto.of(
                cursorRequest.next(lastKey, DEFAULT_SIZE),
                applicantNumber,
                applicants,
                ratings);
    }

    private List<Applicant> loadApplicants(CursorRequest cursorRequest, Long postId, Long userId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);

        if (!cursorRequest.hasKey()) {
            return applicantRepository.findAllByPostIdAndUserIdNotOrderByIdDesc(postId, userId, pageable);
        } else {
            return applicantRepository.findAllByPostIdAndUserIdNotLessThanOrderByIdDesc(cursorRequest.key(), postId, userId, pageable);
        }
    }

    @Transactional
    public void create(Long userId, Long postId) {
        User user = getUser(userId);
        Post post = getPost(postId);

        if (isMatchedUser(userId, post))
            throw new CustomException(ErrorCode.APPLICANT_PERMISSION_DENIED, "본인의 모집글에 신청할 수 없습니다.");

        applicantRepository.findByUserIdAndPostId(userId, postId).ifPresent(applicant -> {
            throw new CustomException(ErrorCode.APPLICANT_PERMISSION_DENIED, "이미 신청된 사용자입니다.");
        });

        if (isPastDueTime(post)) {
            throw new CustomException(ErrorCode.APPLICANT_PERMISSION_DENIED, "이미 마감된 모집글입니다.");
        }

        Applicant applicant = Applicant.builder()
                .user(user)
                .post(post)
                .build();
        applicantRepository.save(applicant);
    }

    @Transactional
    public void accept(Long userId, Long applicantId, ApplicantRequest.UpdateDto requestDto) {
        Applicant applicant = getApplicantWithPost(applicantId);

        if (!(Objects.equals(userId, applicant.getPost().getUser().getId())))
            throw new CustomException(ErrorCode.APPLICANT_ACCEPT_PERMISSION_DENIED);

        applicant.updateStatus(requestDto.status());
    }

    @Transactional
    public void reject(Long userId, Long applicantId) {
        Applicant applicant = getApplicantWithPost(applicantId);
        var isPostOwner = Objects.equals(userId, applicant.getPost().getUser().getId());
        var isApplicantOwner = Objects.equals(userId, applicant.getUser().getId());

        if (!(isApplicantOwner || isPostOwner))
            throw new CustomException(ErrorCode.APPLICANT_REJECT_PERMISSION_DENIED);

        applicantRepository.delete(applicant);
    }

    public ApplicantResponse.CheckStatusDto checkStatus(Long userId, Long postId) {
        Applicant applicant = applicantRepository.findByUserIdAndPostId(userId, postId).orElse(null);
        boolean isApplicantPresent = applicant != null;
        Long applicantId = isApplicantPresent ? applicant.getId() : null;
        return new ApplicantResponse.CheckStatusDto(applicantId, isApplicantPresent, isApplicantPresent && applicant.getStatus());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND)
        );
    }

    private Applicant getApplicantWithPost(Long applicantId) {
        return applicantRepository.findByIdJoinFetchPost(applicantId).orElseThrow(
                () -> new CustomException(ErrorCode.APPLICANT_NOT_FOUND)
        );
    }

    private Long getLastKey(List<Applicant> applicants) {
        return applicants.isEmpty() ? CursorRequest.NONE_KEY : applicants.get(applicants.size() - 1).getId();
    }

    private boolean isPastDueTime(Post post) {
        return post.getIsClose() || LocalDateTime.now().isAfter(post.getDueTime());
    }

    private boolean isMatchedUser(Long userId, Post post) {
        return Objects.equals(post.getUser().getId(), userId);
    }

    @Transactional
    public void rateUser(Long userId, Long applicantId, ApplicantRequest.RateDto requestDto) {
        var applicant = getApplicantWithPost(applicantId);

        var isMine = Objects.equals(applicant.getUser().getId(), userId);
        var isAccept = applicant.getStatus();
        var isAvailable = applicant.getPost().getIsClose() && LocalDateTime.now().isAfter(applicant.getPost().getStartTime());

        if (!isMine) throw new CustomException(ErrorCode.RATING_PERMISSION_DENIED, "자신의 신청이 아닙니다.");
        if (!(isAccept && isAvailable))
            throw new CustomException(ErrorCode.RATING_REQUEST_FAILED, "등록할 수 있는 상태가 아닙니다.");

        var targetApplicant = applicantRepository.findByUserIdAndPostId(requestDto.targetId(), applicant.getPost().getId()).orElse(null);

        var checkTarget = targetApplicant != null && !Objects.equals(targetApplicant.getUser().getId(), userId) && targetApplicant.getStatus();
        if (!checkTarget) {
            throw new CustomException(ErrorCode.RATING_REQUEST_FAILED, "잘못된 요청입니다.");
        }

        userRateRepository.findAllByApplicantId(applicantId).forEach(userRate -> {
            if (Objects.equals(userRate.getUser().getId(), requestDto.targetId()))
                throw new CustomException(ErrorCode.RATING_REQUEST_FAILED, "이미 등록되었습니다.");
        });

        var targetUser = userRepository.findById(requestDto.targetId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        var rate = UserRate.builder()
                .applicant(applicant)
                .user(targetUser)
                .starCount(requestDto.rating())
                .build();

        userRateRepository.save(rate);
    }
}
