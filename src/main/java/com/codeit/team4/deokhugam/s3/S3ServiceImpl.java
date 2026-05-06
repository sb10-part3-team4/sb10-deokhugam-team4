package com.codeit.team4.deokhugam.s3;

import com.codeit.team4.deokhugam.global.config.S3Properties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.io.File;
import java.io.IOException;
import java.util.Map;
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
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
@Profile({"local", "prod"})
@Slf4j
public class S3ServiceImpl implements S3Service {

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp"
    );

    private final S3Client s3Client;    // AWS S3와의 통신을 위한 클라이언트
    private final S3Properties s3Properties;    // 설정 파일(yml)에서 가져온 속성값

    @Override
    public String upload(MultipartFile file) {
        // 1. 파일이 비어있는지 체크
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.S3_EMPTY_FILE_ERROR, "parameter=file");
        }

        // 2. 이미지 파일인지 확장자 및 타입 검증
        String contentType = file.getContentType();
        String ext = ALLOWED_IMAGE_TYPES.get(contentType);
        if (ext == null) {
            throw new BusinessException(ErrorCode.S3_INVALID_FILE_TYPE_ERROR,
                    "Type=" + contentType);
        }

        // 원본 파일명 대신 서버 측 확장자 매핑값 사용
        String key = "thumbnails/" + UUID.randomUUID() + "." + ext;

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

        } catch (S3Exception | IOException e) {
            throw new BusinessException(ErrorCode.S3_UPLOAD_ERROR, "key=" + key);
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // URL에서 S3 Key 부분만 파싱하여 추출
            String path = java.net.URI.create(fileUrl).getPath();
            String key = path.startsWith("/") ? path.substring(1) : path;
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

    @Override
    public String upload(File file, String dirName, String s3FileName) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new BusinessException(ErrorCode.S3_EMPTY_FILE_ERROR, "업로드할 파일이 존재하지 않습니다.");
        }

        if (dirName == null || dirName.isBlank() || s3FileName == null || s3FileName.isBlank()) {
            throw new BusinessException(ErrorCode.S3_INVALID_PATH_ERROR, "경로/파일 이름이 비어 있습니다.");
        }

        String key = dirName + "/" + s3FileName;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(key)
                    .contentType("text/plain")
                    .contentLength(file.length()) // 일관성 유지
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(file));

            log.info("S3 파일 업로드 완료: key={}", key);
            return generateUrl(key);

        } catch (S3Exception e) {
            log.error("S3 파일 업로드 실패: key={}", key, e);
            throw new BusinessException(ErrorCode.S3_UPLOAD_ERROR, "key=" + key);
        }
    }

    // 파일 접근을 위한 정적 URL 생성
    private String generateUrl(String key) {
        return "https://" + s3Properties.getBucketName()
                + ".s3." + s3Properties.getRegion()
                + ".amazonaws.com/" + key;
    }
}
