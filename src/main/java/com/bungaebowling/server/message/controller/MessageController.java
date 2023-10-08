package com.bungaebowling.server.message.controller;
import com.bungaebowling.server._core.security.CustomUserDetails;
import com.bungaebowling.server._core.utils.ApiUtils;
import com.bungaebowling.server._core.utils.CursorRequest;
import com.bungaebowling.server.message.dto.MessageRequest;
import com.bungaebowling.server.message.dto.MessageResponse;
import com.bungaebowling.server.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    // 대화방(쪽지) 목록 조회
    @GetMapping("/opponents")
    public ResponseEntity<?> getOpponents(CursorRequest cursorRequest,@AuthenticationPrincipal CustomUserDetails userDetails) {
        MessageResponse.GetOpponentsDto response = messageService.getOpponents(cursorRequest,userDetails.getId());
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    // 일대일 대화방 쪽지 조회
    @GetMapping("/opponents/{opponentId}")
    public ResponseEntity<?> getMessagesByOpponentId(CursorRequest cursorRequest, @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long opponentId) {
        MessageResponse.GetMessagesDto response = messageService.getMessagesByOpponentId(cursorRequest,userDetails.getId(),opponentId);
        return ResponseEntity.ok().body(ApiUtils.success(response));
    }

    // 쪽지 보내기
    @PostMapping("/opponents/{opponentId}")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid MessageRequest.SendMessageDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long opponentId)
     {
         messageService.sendMessage(requestDto,userDetails.getId(),opponentId);
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    // 쪽지함 삭제
    @DeleteMapping("/opponents/{opponentId}")
    public ResponseEntity<?> deleteMessagesByOpponentId(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long opponentId) {
        messageService.deleteMessagesByOpponentId(userDetails.getId(),opponentId);
        return ResponseEntity.ok().body(ApiUtils.success());
    }

    // 쪽지 개별 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessageById( @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long messageId) {
        messageService.deleteMessageById(userDetails.getId(),messageId);
        return ResponseEntity.ok().body(ApiUtils.success());
    }

}
