package com.codeit.team4.deokhugam.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("test")
public class OcrSpaceClientStub implements OcrSpaceClient{

    @Override
    public String extractText(MultipartFile image) {
        return "";
    }
}
