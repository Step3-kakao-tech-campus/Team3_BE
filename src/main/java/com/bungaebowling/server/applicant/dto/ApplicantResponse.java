package com.bungaebowling.server.applicant.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.user.User;

import java.util.List;
import java.util.stream.IntStream;

public class ApplicantResponse {

    public record GetApplicantsDto(
            CursorRequest nextCursorRequest,
            Long applicantNumber,
            List<ApplicantDto> applicants
    ) {
        public static GetApplicantsDto of(CursorRequest nextCursorRequest, Long applicantNumber, List<Applicant> applicants, List<Double> ratings) {
            return new GetApplicantsDto(
                    nextCursorRequest,
                    applicantNumber,
                    IntStream.range(0, applicants.size())
                            .mapToObj(index -> new ApplicantDto(applicants.get(index), ratings.get(index))).toList()
            );
        }

        public record ApplicantDto(
                Long id,
                UserDto user,
                Boolean status
        ) {

            public ApplicantDto(Applicant applicant, Double rating) {
                this(
                        applicant.getId(),
                        new UserDto(applicant.getUser(), rating),
                        applicant.getStatus()
                );
            }

            public record UserDto(
                    Long id,
                    String name,
                    String profileImage,
                    Double rating
            ) {
                public UserDto(User user, Double rating) {
                    this(
                            user.getId(),
                            user.getName(),
                            user.getImgUrl(),
                            rating
                    );
                }
            }
        }
    }

    public record CheckStatusDto(
            Long applicantId,
            Boolean isApplied, //신청 여부
            Boolean isAccepted //수락 여부
    ) {}
}
