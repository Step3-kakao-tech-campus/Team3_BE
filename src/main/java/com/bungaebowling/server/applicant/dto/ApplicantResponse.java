package com.bungaebowling.server.applicant.dto;

import com.bungaebowling.server.applicant.Applicant;

import java.util.List;

public class ApplicantResponse {

    public record GetApplicantsDto(
            Integer applicantNumber,
            List<ApplicantDto> applicants
    ) {
        public static GetApplicantsDto mapToGetApplicantsDto(Integer applicantNumber, List<Applicant> applicants){
            return new GetApplicantsDto(applicantNumber, applicants.stream().map(applicant -> new ApplicantDto(
                    applicant.getId(),
                    "이름", //applicant.getUser().getName(),
                    "이미지 url", //applicant.getUser().getImgUrl(),
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
