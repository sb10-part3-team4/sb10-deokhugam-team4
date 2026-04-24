package com.codeit.team4.deokhugam.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("test")
public class OcrSpaceClientStub implements OcrSpaceClient {

    // 기본값: 성공 경로를 재현할 수 있도록 유효한 13자리 ISBN 포함
    private String fixedText = "ISBN 978-89-6540-260-2";

    @Override
    public String extractText(MultipartFile image) {
        return fixedText;
    }
}