package com.bungaebowling.server._core.errors.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "인증되지 않았습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 검증 실패"),

    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "최대 첨부 사이즈를 초과하였습니다."),
    FILE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "파일은 최대 첨부 수를 초과하였습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패하였습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패하였습니다."),
    INVALID_FILE_UPLOAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 파일 업로드 요청입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 확장자입니다."),

    SCORE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "점수 등록에 실패하였습니다."),
    SCORE_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "점수 정보에 대한 수정 권한이 없습니다."),
    SCORE_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "점수 정보에 대한 삭제 권한이 없습니다."),
    SCORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 점수입니다."),
    SCORE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 점수이미지 입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 모집글입니다."),
    POST_NOT_CLOSE(HttpStatus.FORBIDDEN, "모집글이 마감되지 않았습니다."),
    POST_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "모집글에 대한 수정 권한이 없습니다."),
    POST_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "모집글에 대한 삭제 권한이 없습니다."),
    POST_CLOSE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "모집글에 대한 마감 권한이 없습니다."),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    REPLY_TO_COMMENT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "대댓글을 작성할 수 없는 댓글입니다."),
    COMMENT_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "본인의 댓글만 수정 가능합니다."),
    COMMENT_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "본인의 댓글만 삭제 가능합니다."),

    MESSAGE_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "본인의 쪽지만 삭제 가능합니다."),
    MESSAGE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "본인과 쪽지 대화를 할 수 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쪽지입니다."),
    APPLICANT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신청입니다."),
    APPLICANT_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "신청에 대한 권한이 없습니다."),
    APPLICANT_REJECT_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "작성자만 거절 가능합니다."),
    APPLICANT_ACCEPT_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "작성자만 수락 가능합니다."),
    RATING_PERMISSION_DENIED(HttpStatus.BAD_REQUEST, "권한이 없습니다."),
    RATING_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "잘못된 평점 요청입니다."),

    INVALID_REQUEST_DATA(HttpStatus.BAD_REQUEST, "올바른 양식이 아닙니다."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 행정구역입니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    USER_NAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다."),

    EMAIL_SEND_LIMIT_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 이메일 전송 한도가 초과되었습니다. 내일 다시 시도해주세요."),
    PLACE_DETAILS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 볼링장입니다."),
    PLACE_DETAILS_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "볼링장 데이터 변환에 실패하였습니다."),
    UNKNOWN_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알수없는 서버 내부 에러입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
