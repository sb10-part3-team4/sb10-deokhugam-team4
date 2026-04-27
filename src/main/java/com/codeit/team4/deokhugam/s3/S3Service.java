package com.codeit.team4.deokhugam.s3;

import java.io.File;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String upload(MultipartFile file);

    // 서버 로컬에 존재하는 File 객체(로그) 업로드 용
    String upload(File file, String dirName, String s3FileName);

    void delete(String fileUrl);
}
