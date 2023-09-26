package com.bungaebowling.server.applicant.controller;

import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.cursor.CursorRequest;
import com.bungaebowling.server._core.utils.cursor.PageCursor;
import com.bungaebowling.server.applicant.dto.ApplicantRequest;
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
    public ResponseEntity<?> getApplicants(@PathVariable Long postId, CursorRequest cursorRequest,
                                           @AuthenticationPrincipal CustomUserDetails userDetails){
        PageCursor<ApplicantResponse.GetApplicantsDto> getApplicantsDto = applicantService.getApplicants(userDetails.getId(), postId, cursorRequest);
        var response = ApiUtils.success(getApplicantsDto);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails userDetails){
        applicantService.create(userDetails.getId(), postId);
        return ResponseEntity.ok(ApiUtils.success());
    }

    @PutMapping("/{applicantId}")
    public ResponseEntity<?> update(@PathVariable Long applicantId, ApplicantRequest.UpdateDto requestDto,
                                    @AuthenticationPrincipal CustomUserDetails userDetails){
        applicantService.update(userDetails.getId(), applicantId, requestDto);
        return ResponseEntity.ok(ApiUtils.success());
    }

    @DeleteMapping("/{applicantId}")
    public ResponseEntity<?> delete(@PathVariable Long applicantId, @AuthenticationPrincipal CustomUserDetails userDetails){
        applicantService.delete(userDetails.getId(), applicantId);
        return ResponseEntity.ok(ApiUtils.success());
    }
}
