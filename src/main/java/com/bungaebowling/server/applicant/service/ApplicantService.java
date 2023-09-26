package com.bungaebowling.server.applicant.service;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server._core.utils.cursor.PageCursor;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.applicant.dto.ApplicantResponse;
import com.bungaebowling.server.applicant.repository.ApplicantRepository;
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

    public PageCursor<ApplicantResponse.GetApplicantsDto> getApplicants(Long userId, Long postId, CursorRequest cursorRequest){
        Pageable pageable = PageRequest.of(0, cursorRequest.size());
        int applicantNumber = applicantRepository.getApplicantNumber(postId);
        List<Applicant> applicants = applicantRepository.findApplicantByPostId(pageable, userId, postId);
        Long key = 1L;
        return new PageCursor<>(cursorRequest.next(key), ApplicantResponse.GetApplicantsDto.mapToGetApplicantsDto(applicantNumber, applicants));
    }

    public void apply(){

    }

    public void accept(){

    }

    public void reject(){

    }
}
