package com.bungaebowling.server.applicant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts/{postId}/applicants")
public class ApplicantController {

    @GetMapping
    public ResponseEntity<?> getApplicants(){
        return ResponseEntity.ok().body();
    }

    @PostMapping
    public ResponseEntity<?> apply(){
        return ResponseEntity.ok().body();
    }

    @PutMapping("/{applicantId}")
    public ResponseEntity<?> accept(){
        return ResponseEntity.ok().body();
    }

    @DeleteMapping("/{applicantId}")
    public ResponseEntity<?> reject(){
        return ResponseEntity.ok().body();
    }
}
