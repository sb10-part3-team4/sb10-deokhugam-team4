package com.codeit.team4.deokhugam.global.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.s3.S3Service;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LogBackupServiceTest {

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private LogBackupService logBackupService;

    @TempDir
    Path tempDir; // JUnit5가 제공하는 안전한 임시 디렉토리

    @BeforeEach
    void setUp() {
        // @Value 로 주입되는 프로퍼티 값들을 리플렉션으로 주입
        ReflectionTestUtils.setField(logBackupService, "logDirectory", tempDir.toString());
        ReflectionTestUtils.setField(logBackupService, "s3UploadDirectory", "logs");
    }

    @Test
    @DisplayName("S3 업로드 후 로컬 파일 삭제 성공")
    void backupAndCleanUp_Success() throws IOException {
        // given
        String yesterdayDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String fileName = "app-" + yesterdayDate + ".log";
        File mockLogFile = new File(tempDir.toFile(), fileName);
        mockLogFile.createNewFile(); // 임시 파일 생성

        // when
        logBackupService.backupAndCleanUpYesterdayLog();

        // then
        verify(s3Service).uploadFile(eq(mockLogFile), eq("logs"), anyString());
        assertThat(mockLogFile.exists()).isFalse();
    }

    @Test
    @DisplayName("어제 날짜의 로그 파일이 존재하지 않을 시 업로드 실패")
    void backupAndCleanUp_NoFile_FailedUpload() {
        // given

        // when
        logBackupService.backupAndCleanUpYesterdayLog();

        // then
        verify(s3Service, never()).uploadFile(any(), any(), any());
    }

    @Test
    @DisplayName("S3 업로드 중 예외가 발생하면 로컬 파일은 삭제 실패")
    void backupAndCleanUp_UploadException_KeepsLocalFile() throws IOException {
        // given
        String yesterdayDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        File mockLogFile = new File(tempDir.toFile(), "app-" + yesterdayDate + ".log");
        mockLogFile.createNewFile();

        doThrow(new RuntimeException("AWS S3 통신 에러"))
                .when(s3Service).uploadFile(any(), any(), any());

        // when
        logBackupService.backupAndCleanUpYesterdayLog();

        // then
        assertThat(mockLogFile.exists()).isTrue();
    }
}