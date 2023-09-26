package com.bungaebowling.server.applicant.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server._core.utils.cursor.PageCursor;
import com.bungaebowling.server.applicant.dto.ApplicantResponse;
import com.bungaebowling.server.applicant.service.ApplicantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts/{postId}/applicants")
public class ApplicantController {

    private final ApplicantService applicantService;

    @GetMapping
    public ResponseEntity<?> getApplicants(@PathVariable Long postId,
                                           @AuthenticationPrincipal CustomUserDetails userDetails,
                                           CursorRequest cursorRequest){
        PageCursor<ApplicantResponse.GetApplicantsDto> getApplicantsDto = applicantService.getApplicants(userDetails.getId(), postId, cursorRequest);
        var response = ApiUtils.success(getApplicantsDto);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<?> apply(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        applicantService.apply();
        return ResponseEntity.ok().body();
    }

    @PutMapping("/{applicantId}")
    public ResponseEntity<?> accept(@PathVariable Long postId, @PathVariable Long applicantId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails){
        applicantService.accept();
        return ResponseEntity.ok().body();
    }

    @DeleteMapping("/{applicantId}")
    public ResponseEntity<?> reject(@PathVariable Long postId, @PathVariable Long applicantId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails){
        applicantService.reject();
        return ResponseEntity.ok().body();
    }
}
