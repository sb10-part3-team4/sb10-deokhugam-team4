package com.codeit.team4.deokhugam.s3;

import java.io.File;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("test")
public class S3ServiceStub implements S3Service {

    @Override
    public String upload(MultipartFile file) {
        return "https://test-bucket.s3.ap-northeast-2.amazonaws.com/thumbnails/stub";
    }

    @Override
    public void delete(String fileUrl) {
    }

    @Override
    public String upload(File file, String dirName, String s3FileName) {
        return "https://test-bucket.s3.ap-northeast-2.amazonaws.com/" + dirName + "/" + s3FileName;
    }
}
