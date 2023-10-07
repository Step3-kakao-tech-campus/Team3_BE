package com.bungaebowling.server._core.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bungaebowling.server._core.errors.exception.client.Exception404;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class AwsS3Service {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.servlet.multipart.max-file-size}")
    private Long fileMaxSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private Long totalFilesMaxSize;

    // 점수 단일 파일용
    public String uploadScoreFile(String userName, Long postId, String category, MultipartFile multipartFile) {
        String fileName = CommonUtils.buildScoreFileName(userName, postId, category, Objects.requireNonNull(multipartFile.getOriginalFilename()));
        uploadFileToS3(fileName, multipartFile);

        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    // 단일 파일용 - 알아서 잘 custom해서 사용하면 됨
    public String uploadFile(String userName, String category, MultipartFile multipartFile) {
        String fileName = CommonUtils.buildFileName(category, Objects.requireNonNull(multipartFile.getOriginalFilename()));
        uploadFileToS3(fileName, multipartFile);

        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    // 점수 - 다중 파일용
    public List<String> uploadMultiFile(String userName, Long postId, String category, List<MultipartFile> multipartFiles) {
        List<String> imageUrls = new ArrayList<>();
        Long totalSize = 0L;

        for (MultipartFile multipartFile : multipartFiles) {
            if(imageUrls.size() > 10) {
                throw new Exception404("업로드 할 수 있는 최대 파일 개수는 10개 입니다.");
            }

            if(multipartFile.getSize() > fileMaxSize) {
                throw new Exception404("최대 10MB까지의 파일을 첨부 할 수 있습니다.");
            }

            totalSize += multipartFile.getSize();

            if (totalSize > totalFilesMaxSize) {
                throw new Exception404("최대 총 100MB의 파일을 첨부 할 수 있습니다");
            }

            String fileName = CommonUtils.buildScoreFileName(userName, postId, category, Objects.requireNonNull(multipartFile.getOriginalFilename()));
            uploadFileToS3(fileName, multipartFile);
        }

        return imageUrls;
    }

    // s3에 파일을 올리는 로직
    private void uploadFileToS3(String fileName, MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try(InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new Exception404("파일 업로드에 실패하였습니다.");
        }
    }
}
