package com.bungaebowling.server.applicant.service;

import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server._core.utils.cursor.PageCursor;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.applicant.dto.ApplicantRequest;
import com.bungaebowling.server.applicant.dto.ApplicantResponse;
import com.bungaebowling.server.applicant.repository.ApplicantRepository;
import com.bungaebowling.server.post.Post;
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
public class ApplicantService {

    public static final int DEFAULT_SIZE = 20;

    private final ApplicantRepository applicantRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PageCursor<ApplicantResponse.GetApplicantsDto> getApplicants(Long userId, Long postId, CursorRequest cursorRequest){
        Post post = getPost(postId);
        int applicantNumber = applicantRepository.countByPostId(post.getId());
        List<Applicant> applicants = loadApplicants(userId, cursorRequest, post.getId());
        Long lastKey = applicants.isEmpty() ? CursorRequest.NONE_KEY : applicants.get(applicants.size() - 1).getId();
        return new PageCursor<>(cursorRequest.next(lastKey), ApplicantResponse.GetApplicantsDto.of(applicantNumber, applicants));
    }

    private List<Applicant> loadApplicants(Long userId, CursorRequest cursorRequest, Long postId) {
        int size = cursorRequest.hasSize() ? cursorRequest.size() : DEFAULT_SIZE;
        Pageable pageable = PageRequest.of(0, size);

        if(!cursorRequest.hasKey()){
            return applicantRepository.findAllByUserIdAndPostIdOrderByIdDesc(userId, postId, pageable);
        }else{
            return applicantRepository.findAllByUserIdAndPostIdLessThanOrderByIdDesc(cursorRequest.key(), userId, postId, pageable);
        }
    }

    @Transactional
    public void create(Long userId, Long postId){
        User user = getUser(userId);
        Post post = getPost(postId);

        //TODO: 게시글 작성자 신청 금지

        //신청 중복 확인
        applicantRepository.findByUserIdAndPostId(userId, postId).ifPresent(applicant -> {
            throw new Exception400("이미 신청된 사용자입니다.");
        });

        Applicant applicant = Applicant.builder().user(user).post(post).build();
        applicantRepository.save(applicant);
    }

    @Transactional
    public void accept(Long applicantId, ApplicantRequest.UpdateDto requestDto){
        //TODO: 게시글 작성자만 수락 가능

        Applicant applicant = getApplicant(applicantId);
        applicant.updateStatus(requestDto.status());
    }

    @Transactional
    public void reject(Long applicantId){
        Applicant applicant = getApplicant(applicantId);
        applicantRepository.deleteById(applicant.getId());
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

    private Applicant getApplicant(Long applicantId) {
        return applicantRepository.findById(applicantId).orElseThrow(
                () -> new Exception404("존재하지 않는 신청입니다.")
        );
    }
}