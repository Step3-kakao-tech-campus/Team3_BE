package com.bungaebowling.server.applicant.service;

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

    private final ApplicantRepository applicantRepository;
    private final PostRepository postRepository;

    public PageCursor<ApplicantResponse.GetApplicantsDto> getApplicants(Long userId, Long postId, CursorRequest cursorRequest){
        Pageable pageable = PageRequest.of(0, cursorRequest.size());
        int applicantNumber = applicantRepository.getApplicantNumber(postId);
        List<Applicant> applicants = applicantRepository.findApplicantByPostId(pageable, userId, postId);
        Long key = 1L;
        return new PageCursor<>(cursorRequest.next(key), ApplicantResponse.GetApplicantsDto.mapToGetApplicantsDto(applicantNumber, applicants));
    }

    @Transactional
    public void create(Long userId, Long postId){
        User user = null; //user repository 추가되면 수정 예정
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new Exception404("존재하지 않는 모집글입니다.")
        );
        Applicant applicant = Applicant.builder().user(user).post(post).build();
        applicantRepository.save(applicant);
    }

    @Transactional
    public void update(Long userId, Long applicantId, ApplicantRequest.UpdateDto requestDto){
        Applicant applicant = applicantRepository.findById(applicantId).orElseThrow(
                () -> new Exception404("존재하지 않는 신청입니다.")
        );
        applicantRepository.updateStatus(applicant.getId(), requestDto.status());
    }

    @Transactional
    public void delete(Long userId, Long applicantId){
        Applicant applicant = applicantRepository.findById(applicantId).orElseThrow(
                () -> new Exception404("존재하지 않는 신청입니다.")
        );
        applicantRepository.deleteById(applicant.getId());
    }
}
