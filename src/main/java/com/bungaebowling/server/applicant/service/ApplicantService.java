package com.bungaebowling.server.applicant.service;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.applicant.dto.ApplicantResponse;
import com.bungaebowling.server.applicant.repository.ApplicantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ApplicantService {

    private final ApplicantRepository applicantRepository;

    public ApplicantResponse.GetApplicantsDto getApplicants(Long userId, Long postId, CursorRequest cursorRequest){
        int applicantNumber = applicantRepository.getApplicantNumber(postId);
        List<Applicant> applicants = applicantRepository.findApplicantByPostId(userId, postId);
        List<ApplicantResponse.GetApplicantsDto.ApplicantDto> applicantDtos = applicants.stream().map(applicant -> new ApplicantResponse.GetApplicantsDto.ApplicantDto(
                applicant.getId(),
                applicant.getUser().getName(),
                applicant.getUser().getImgUrl(),
                1.0 //UserRate 생성된 후 수정
        )).collect(Collectors.toList());
        return new ApplicantResponse.GetApplicantsDto(cursorRequest, applicantNumber, applicantDtos);
    }

    public void apply(){

    }

    public void accept(){

    }

    public void reject(){

    }
}
