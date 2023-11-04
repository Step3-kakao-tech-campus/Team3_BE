package com.bungaebowling.server._core.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bungaebowling.server._core.errors.exception.CustomException;
import com.bungaebowling.server._core.errors.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsS3Service {
    @Autowired
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.servlet.multipart.max-file-size}")
    private Long fileMaxSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private Long totalFilesMaxSize;

    // 점수 단일 파일용
    public String uploadScoreFile(Long userId, Long postId, String category, LocalDateTime time, MultipartFile multipartFile) {
        String fileName = CommonUtils.buildScoreFileName(userId, postId, category, time, Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String safeFileName = fileWhiteList(fileName);

        uploadFileToS3(safeFileName, multipartFile);

        return safeFileName;
    }

    //프로필 이미지 등록
    public String uploadProfileFile(Long userId, String category, LocalDateTime time, MultipartFile multipartFile) {
        String fileName = CommonUtils.buildProfileFileName(userId, category, time, Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String safeFileName = fileWhiteList(fileName);

        uploadFileToS3(safeFileName, multipartFile);

        return safeFileName;
    }

    // 단일 파일용 - 알아서 잘 custom해서 사용하면 됨
    public String uploadFile(Long userId, String category, MultipartFile multipartFile) {
        String fileName = CommonUtils.buildFileName(category, Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String safeFileName = fileWhiteList(fileName);

        uploadFileToS3(safeFileName, multipartFile);

        return safeFileName;
    }

    // 점수 - 다중 파일용
    public List<String> uploadMultiFile(Long userId, Long postId, String category, LocalDateTime time, List<MultipartFile> multipartFiles) {
        List<String> imageUrls = new ArrayList<>();
        Long totalSize = 0L;

        if (multipartFiles.size() > 10) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_LIMIT_EXCEEDED, "파일은 최대 10개까지 올릴 수 있습니다.");
        }

        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.getSize() > fileMaxSize) {
                throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED, "최대 10MB의 파일을 첨부 할 수 있습니다");
            }

            totalSize += multipartFile.getSize();

            if (totalSize > totalFilesMaxSize) {
                throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED, "최대 총 100MB의 파일을 첨부 할 수 있습니다");
            }
        }

        for (MultipartFile multipartFile : multipartFiles) {
            String fileName = CommonUtils.buildScoreFileName(userId, postId, category, time, Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String safeFileName = fileWhiteList(fileName);

            imageUrls.add(safeFileName);
            uploadFileToS3(safeFileName, multipartFile);
        }

        return imageUrls;
    }

    // 이미지 접속 url 반환
    public String getImageAccessUrl(String fileName) {
        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    // s3에 파일을 올리는 로직
    private void uploadFileToS3(String fileName, MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    // s3파일 삭제하기
    public void deleteFile(String fileName) {
        try {
            amazonS3Client.deleteObject(bucketName, fileName);
        } catch (AmazonS3Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // 파일 확장자 검사
    private String fileWhiteList(String fileName) {
        // 대소문자 구별안하게
        if (fileName == null) {
            throw new CustomException(ErrorCode.INVALID_FILE_UPLOAD_REQUEST);
        }
        String caseInSensitiveFileName = fileName.toLowerCase();

        if (
                caseInSensitiveFileName.endsWith(".png") ||
                        caseInSensitiveFileName.endsWith(".gif") ||
                        caseInSensitiveFileName.endsWith(".jpeg") ||
                        caseInSensitiveFileName.endsWith(".jpg")
        ) {
            return caseInSensitiveFileName;
        } else {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }
}
