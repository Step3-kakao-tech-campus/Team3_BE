package com.bungaebowling.server.applicant.dto;

import jakarta.validation.constraints.NotBlank;

public class ApplicantRequest {

    public record UpdateDto(
            @NotBlank
            Boolean status
    ){}
}
