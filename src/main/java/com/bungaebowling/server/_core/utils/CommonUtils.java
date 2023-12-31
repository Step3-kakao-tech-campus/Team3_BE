package com.bungaebowling.server._core.utils;

import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommonUtils {
    private static final String FILE_EXTENSION_SEPARATOR = ".";
    private static final String CATEGORY_PREFIX = "/";
    private static final String TIME_SEPARATOR = "_";
    private static final String WORD_SEPARATOR = "_";

    // 점수 등록용
    public static String buildScoreFileName(Long userId, Long postId, String category, LocalDateTime time, String originalFileName) {
        int fileExtensionIndex = getFileExtensionIndex(originalFileName);
        String fileExtension = originalFileName.substring(fileExtensionIndex); // 파일 확장자
        String now = String.valueOf(time); // 파일 업로드 시간

        // 작성자/게시글ID/score/파일명/파일업로드시간.확장자 -> 이런 방식으로 저장됨
        return "user" + WORD_SEPARATOR + userId + CATEGORY_PREFIX + postId + CATEGORY_PREFIX + category + CATEGORY_PREFIX + now + fileExtension;
    }

    //프로필 등록
    public static String buildProfileFileName(Long userId, String category, LocalDateTime time, String originalFileName) {
        int fileExtensionIndex = getFileExtensionIndex(originalFileName);
        String fileExtension = originalFileName.substring(fileExtensionIndex); // 파일 확장자
        String now = String.valueOf(time); // 파일 업로드 시간

        //작성자(user_1)/profile/파일명/파일업로드시간.확장자
        return "user" + WORD_SEPARATOR + userId + CATEGORY_PREFIX + category + CATEGORY_PREFIX  + now + fileExtension;
    }

    private static int getFileExtensionIndex(String originalFileName) {
        int fileExtensionIndex = originalFileName.lastIndexOf(FILE_EXTENSION_SEPARATOR); // 파일 확장자 구분선

        if (fileExtensionIndex == -1) {
            throw new CustomException(ErrorCode.FILE_REQUEST_FAILED);
        }
        return fileExtensionIndex;
    }

    // 단일 파일용 Template
    public static String buildFileName(String category, String originalFileName) {
        int fileExtensionIndex = originalFileName.lastIndexOf(FILE_EXTENSION_SEPARATOR); // 파일 확장자 구분선
        String fileExtension = originalFileName.substring(fileExtensionIndex); // 파일 확장자
        String fileName = originalFileName.substring(0, fileExtensionIndex); // 파일 이름
        String now = String.valueOf(System.currentTimeMillis()); // 파일 업로드 시간

        return category + CATEGORY_PREFIX + fileName + TIME_SEPARATOR + now + fileExtension;
    }
}
