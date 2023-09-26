package com.bungaebowling.server.applicant.dto;

import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicantResponse {

    public record GetApplicantsDto(
            CursorRequest nextCursorRequest,
            Integer applicantNumber,
            List<ApplicantDto> applicants
    ) {
        public record ApplicantDto(
                Long id,
                String userName,
                String profileImage,
                Double rating
        ) {

        }
    }
}
