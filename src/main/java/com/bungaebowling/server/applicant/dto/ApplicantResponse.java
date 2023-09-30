package com.bungaebowling.server.applicant.dto;

import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.applicant.Applicant;
import com.bungaebowling.server.user.User;
import com.bungaebowling.server.user.rate.UserRate;

import java.util.List;

public class ApplicantResponse {

    public record GetApplicantsDto(
            CursorRequest nextCursorRequest,
            Long participantNumber,
            Long currentNumber,
            List<ApplicantDto> applicants
    ) {
        public static GetApplicantsDto of(CursorRequest nextCursorRequest, Long participantNumber, Long currentNumber, List<Applicant> applicants){
            return new GetApplicantsDto(
                    nextCursorRequest,
                    participantNumber,
                    currentNumber,
                    applicants.stream().map(ApplicantDto::new).toList()
            );
        }

        public record ApplicantDto(
                Long id,
                UserDto user,
                Boolean status
        ) {

            public ApplicantDto(Applicant applicant) {
                this(
                        applicant.getId(),
                        new UserDto(applicant.getUser()),
                        applicant.getStatus()
                );
            }

            public record UserDto(
                    Long id,
                    String name,
                    String profileImage,
                    Double rating
            ) {
                public UserDto(User user) {
                    this(
                            user.getId(),
                            user.getName(),
                            user.getImgUrl(),
                            user.getUserRates().stream()
                                    .mapToInt(UserRate::getStarCount)
                                    .average().orElse(0.0)
                    );
                }
            }

        }
    }
}
