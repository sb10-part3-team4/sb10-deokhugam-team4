package com.codeit.team4.deokhugam.ocr;

import org.springframework.web.multipart.MultipartFile;

public interface OcrSpaceClient {
    String extractText(MultipartFile image);
}