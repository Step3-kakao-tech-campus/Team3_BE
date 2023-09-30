package com.bungaebowling.server.applicant.dto;

import jakarta.validation.constraints.NotNull;

public class ApplicantRequest {

    public record UpdateDto(
            @NotNull
            Boolean status
    ){}
}
