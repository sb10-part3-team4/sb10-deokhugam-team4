package com.codeit.team4.deokhugam.s3;

import com.codeit.team4.deokhugam.global.config.S3Properties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;    // AWS S3와의 통신을 위한 클라이언트
    private final S3Properties s3Properties;    // 설정 파일(yml)에서 가져온 속성값

    public String upload(MultipartFile file) {
        // 1. 파일이 비어있는지 체크
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.S3_EMPTY_FILE_ERROR, "parameter=file");
        }

        // 2. 이미지 파일인지 확장자 및 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.S3_INVALID_FILE_TYPE_ERROR,
                    "Type=" + contentType);
        }

        // 중복 방지를 위해 파일명 앞에 UUID를 붙여 고유한 키(경로) 생성
        String key = "thumbnails/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // S3에 객체를 저장하기 위한 요청 객체 빌드
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // S3에 실제 데이터 업로드
            s3Client.putObject(request, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            log.info("S3 업로드 완료: key={}", key);
            return generateUrl(key);    // 업로드된 파일의 접근 URL 반환

        } catch (Exception e) {
            log.error("S3 업로드 실패: key={}", key, e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_ERROR, "key=" + key);
        }
    }

    public void delete(String fileUrl) {
        try {
            // URL에서 S3 Key 부분만 파싱하여 추출
            String key = fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + 15);

            // 삭제 요청 객체 생성 및 실행
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .build());
            log.info("S3 삭제 완료: key={}", key);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_DELETE_ERROR, "fileUrl=" + fileUrl);
        }
    }

    // 파일 접근을 위한 정적 URL 생성
    private String generateUrl(String key) {
        return "https://" + s3Properties.getBucketName()
                + ".s3." + s3Properties.getRegion()
                + ".amazonaws.com/" + key;
    }
}