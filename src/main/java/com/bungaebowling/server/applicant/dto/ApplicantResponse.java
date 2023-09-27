package com.bungaebowling.server.applicant.dto;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicantResponse {

    public record GetApplicantsDto(
            Integer applicantNumber,
            List<ApplicantDto> applicants
    ) {
        public static GetApplicantsDto mapToGetApplicantsDto(Integer applicantNumber, List<Applicant> applicants){
            return new GetApplicantsDto(applicantNumber, applicants.stream().map(applicant -> new ApplicantDto(
                    applicant.getId(),
                    applicant.getUser().getName(),
                    applicant.getUser().getImgUrl(),
                    1.0 //UserRate 생성된 후 수정
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