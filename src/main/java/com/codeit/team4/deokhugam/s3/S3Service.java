package com.codeit.team4.deokhugam.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String upload(MultipartFile file);

    void delete(String fileUrl);
}
