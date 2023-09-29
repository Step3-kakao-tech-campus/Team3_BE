package com.bungaebowling.server.applicant.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;

import java.util.List;

public class ApplicantResponse {

    public record GetApplicantsDto(
            CursorRequest nextCursorRequest,
            Integer applicantNumber,
            List<ApplicantDto> applicants
    ) {
        public static GetApplicantsDto of(CursorRequest nextCursorRequest, Integer applicantNumber, List<Applicant> applicants){
            return new GetApplicantsDto(nextCursorRequest, applicantNumber, applicants.stream().map(applicant -> new ApplicantDto(
                    applicant.getId(),
                    applicant.getUser().getName(),
                    applicant.getUser().getImgUrl(),
                    1.0 //TODO: UserRate 생성된 후 수정
            )).toList());
        }

        public record ApplicantDto(
                Long id,
                String userName,
                String profileImage,
                Double rating
        ) {

        }
    }
}
