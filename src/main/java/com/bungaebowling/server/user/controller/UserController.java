package com.bungaebowling.server.user.controller;


import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.security.JwtProvider;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.user.dto.UserRequest;
import com.bungaebowling.server.user.dto.UserResponse;
import com.bungaebowling.server.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    final private UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequest.JoinDto requestDto, Errors errors) throws URISyntaxException {
        var responseDto = userService.join(requestDto);

        var responseCookie = createRefreshTokenCookie(responseDto.refresh());

        return ResponseEntity.ok()
                .header(JwtProvider.HEADER, responseDto.access())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ApiUtils.success(new UserResponse.CreateDto(responseDto.savedId())));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest.LoginDto requestDto, Errors errors) {
        var tokens = userService.login(requestDto);

        var responseCookie = createRefreshTokenCookie(tokens.refresh());

        var response = ApiUtils.success();
        return ResponseEntity.ok().header(JwtProvider.HEADER, tokens.access())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.logout(userDetails.getId());

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", "")
                .maxAge(0)
                .sameSite("None")
                .build();

        var response = ApiUtils.success();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }

    @PostMapping("/authentication")
    public ResponseEntity<?> reIssueTokens(@CookieValue("refreshToken") String refreshToken) {

        var tokens = userService.reIssueTokens(refreshToken);

        var responseCookie = createRefreshTokenCookie(tokens.refresh());

        var response = ApiUtils.success();
        return ResponseEntity.ok().header(JwtProvider.HEADER, tokens.access())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }


    @PostMapping("/email-verification")
    public ResponseEntity<?> sendVerification(@AuthenticationPrincipal CustomUserDetails userDetails) throws MessagingException {
        userService.sendVerificationMail(userDetails.getId());
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @PostMapping("/email-confirm")
    public ResponseEntity<?> confirmEmail(@RequestBody UserRequest.ConfirmEmailDto requestDto, Errors errors) {
        userService.confirmEmail(requestDto);
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(CursorRequest cursorRequest, @RequestParam(value = "name", required = false) String name) {
        UserResponse.GetUsersDto response = userService.getUsers(cursorRequest, name);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        UserResponse.GetUserDto response = userService.getUser(userId);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @GetMapping("/users/mine")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse.GetMyProfileDto response = userService.getMyProfile(userDetails.getId());
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @PutMapping("/users/mine")
    public ResponseEntity<?> updateMyProfile(@RequestParam(required = false) MultipartFile profileImage,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) Long districtId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.updateMyProfile(profileImage, name, districtId, userDetails.getId());
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @GetMapping("/users/{userId}/records")
    public ResponseEntity<?> getUserRecords(@PathVariable Long userId) {
        UserResponse.GetRecordDto response = userService.getRecords(userId);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    @PatchMapping("/users/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserRequest.UpdatePasswordDto requestDto) {
        userService.updatePassword(userDetails.getId(), requestDto);
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    @PostMapping("/password/email-verification")
    public ResponseEntity<?> sendVerificationMailForPasswordReset(@RequestBody UserRequest.SendVerificationMailForPasswordResetDto requestDto) {
        userService.sendVerificationMailForPasswordReset(requestDto);
        return ResponseEntity.ok().body(ApiUtils.success());
    }


    @PostMapping("/password/email-confirm")
    public ResponseEntity<?> confirmEmailAndSendTempPassword(@RequestBody UserRequest.ConfirmEmailAndSendTempPasswordDto requestDto, Errors errors) {
        userService.confirmEmailAndSendTempPassword(requestDto);
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    private static ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // javascript 접근 방지
                .secure(true) // https 통신 강제
                .sameSite("None")
                .maxAge(JwtProvider.getRefreshExpSecond())
                .build();
    }


}
