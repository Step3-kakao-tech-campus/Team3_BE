package com.bungaebowling.server._core.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bungaebowling.server._core.errors.exception.client.Exception400;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import com.bungaebowling.server._core.errors.exception.server.Exception500;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.getSize() > fileMaxSize) {
                throw new Exception404("최대 10MB의 파일을 첨부 할 수 있습니다");
            }

            totalSize += multipartFile.getSize();

            if (totalSize > totalFilesMaxSize) {
                throw new Exception404("최대 총 100MB의 파일을 첨부 할 수 있습니다");
            }
        }

        for (MultipartFile multipartFile : multipartFiles) {
            if(imageUrls.size() > 10) {
                throw new Exception404("업로드 할 수 있는 최대 파일 개수는 10개 입니다.");
            }

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

        try(InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch(IOException e) {
            throw new Exception500("파일 업로드에 실패하였습니다.");
        }
    }

    // s3파일 삭제하기
    public void deleteFile(String fileName) {
        try {
            amazonS3Client.deleteObject(bucketName, fileName);
        } catch(AmazonS3Exception e) {
            throw new Exception500("파일 삭제에 실패하였습니다.");
        }
    }

    // 파일 확장자 검사
    private String fileWhiteList(String fileName) {
        // 대소문자 구별안하게
        String caseInSensitiveFileName = fileName.toLowerCase();
        if(caseInSensitiveFileName == null) {
            throw new Exception400("잘못된 파일 업로드 요청입니다.");
        }

        if(
                caseInSensitiveFileName.endsWith(".png") ||
                caseInSensitiveFileName.endsWith(".gif") ||
                caseInSensitiveFileName.endsWith(".jpeg") ||
                caseInSensitiveFileName.endsWith(".jpg")
        ) {
            return caseInSensitiveFileName;
        } else {
            throw new Exception400("허용되지 않는 파일 확장자입니다.");
        }
    }
}
