package com.bungaebowling.server.applicant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ApplicantRequest {

    public record UpdateDto(
            @NotNull
            Boolean status
    ) {
    }

    public record RateDto(
            @NotNull
            Long targetId,
            @Max(value = 5, message = "1 ~ 5 사이 값만 가능합니다.")
            @Min(value = 1, message = "1 ~ 5 사이 값만 가능합니다.")
            Integer rating
    ) {
    }
}
