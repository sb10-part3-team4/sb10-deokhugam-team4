package com.codeit.team4.deokhugam.global.log;

import static java.net.InetAddress.getLocalHost;
import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;

import com.codeit.team4.deokhugam.s3.S3Service;
import java.io.File;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogBackupService {

    private final S3Service s3Service;

    @Value("${log.backup.dir}")
    private String logDirectory;

    @Value("${log.backup.s3-dir}")
    private String s3UploadDirectory;

    private static final String FILE_PREFIX = "app-";
    private static final String FILE_EXTENSION = ".log";


    public void backupAndCleanUpYesterdayLog() {
        try {
            // 어제 날짜 계산 및 파일명 도출
            LocalDate yesterday = now().minusDays(1);
            String dateString = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String fileName = FILE_PREFIX + dateString + FILE_EXTENSION;

            File logFile = new File(logDirectory, fileName);

            if (!logFile.exists() || !logFile.isFile()) {
                log.warn("백업할 어제 날짜의 로그 파일이 존재하지 않습니다: {}", logFile.getAbsolutePath());
                return;
            }

            String hostName = getHostName();
            String s3FileName = hostName + "_" + fileName;

            log.info("로그 파일 S3 백업을 시작합니다: {} → {}/{}", fileName, s3UploadDirectory, s3FileName);

            // S3 업로드
            s3Service.uploadFile(logFile, s3UploadDirectory, s3FileName);

            log.info("로그 파일 S3 백업 완료: {}", fileName);

            // 업로드 완료 후 로컬 원본 삭제
            boolean isDeleted = logFile.delete();
            if (isDeleted) {
                log.info("로컬 로그 파일 삭제 완료: {}", fileName);
            } else {
                log.error("로컬 로그 파일 삭제 실패: {}", fileName);
            }

        } catch (Exception e) {
            log.error("로그 파일 백업 및 삭제 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }

    private String getHostName() {
        try {
            return getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("호스트명 조회 실패, UUID로 대체합니다.", e);
            return randomUUID().toString();
        }
    }
}