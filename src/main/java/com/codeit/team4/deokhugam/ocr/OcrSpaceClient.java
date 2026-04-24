package com.codeit.team4.deokhugam.ocr;

import com.codeit.team4.deokhugam.book.dto.OcrResponse;
import com.codeit.team4.deokhugam.global.config.OcrProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OcrSpaceClient {

    private final OcrProperties ocrProperties;  // yaml에서 API KEY, URL 관리
    private final RestClient restClient;    // 동기식 API 호출 도구

    // OCR 처리가 가능한 파일 확장자 목록
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "application/pdf"
    );

    // 이미지 파일을 받아 OCR API를 호출하고 추출된 텍스트 전체를 반환
    public String extractText(MultipartFile image) {
        // 파일 형식 검증 (지원하지 않는 형식 방지)
        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.OCR_ISBN_NOT_FOUND, "contentType=" + contentType);
        }
        try {
            // RestClient를 통해 API 서버로 멀티파트 요청 전송
            OcrResponse response = restClient.post()
                    .uri(ocrProperties.getUrl())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(createBody(image))
                    .retrieve()
                    .body(OcrResponse.class);

            // API 응답 결과 검증
            if (response == null || response.IsErroredOnProcessing() ||
                    response.ParsedResults() == null || response.ParsedResults().isEmpty() ||
                    response.ParsedResults().get(0).ParsedText() == null) {

                throw new BusinessException(ErrorCode.OCR_ERROR, "response={}" + response);
            }

            log.info("OCR 텍스트 추출 완료");
            return response.ParsedResults().get(0).ParsedText();    // 추출된 전체 텍스트 반환

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OCR API 호출 실패", e);
            throw new BusinessException(ErrorCode.OCR_ERROR);
        }
    }

    // OCR API 요청에 필요한 폼 데이터 생성
    private MultiValueMap<String, Object> createBody(MultipartFile image) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("apikey", ocrProperties.getKey());
        body.add("language", "eng");
        body.add("isOverlayRequired", "false");
        body.add("file", image.getResource());
        return body;
    }
}