package com.codeit.team4.deokhugam.naver;

import com.codeit.team4.deokhugam.global.config.NaverBookProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverBookClient {
    private final NaverBookProperties naverBookProperties;
    private final RestClient restClient;

    public NaverBookResponse searchByIsbn(String isbn) {
        // ISBN 정규화 (하이픈 제거)
        String normalizedIsbn = isbn.replaceAll("-", "").trim();

        // ISBN 형식 검증 (10자리 또는 13자리 숫자)
        if (!normalizedIsbn.matches("^[0-9]{10}$|^[0-9]{13}$")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "isbn=" + isbn);
        }

        try {
            // 외부 서버로 HTTP 요청 보내기
            NaverBookResponse response = restClient.get()
                    .uri(naverBookProperties.getBookSearchUrl() + "?d_isbn=" + isbn)
                    .header("X-Naver-Client-Id", naverBookProperties.getClientId())
                    .header("X-Naver-Client-Secret", naverBookProperties.getClientSecret())
                    .retrieve()
                    .body(NaverBookResponse.class);

            log.info("네이버 API 도서 검색 완료: isbn={}", isbn);
            return response;
        } catch (RestClientException e) {
            log.error("네이버 API 도서 검색 실패: isbn={}", isbn, e);
            throw new BusinessException(ErrorCode.NAVER_API_ERROR, "isbn=" + isbn);
        }
    }

    public byte[] fetchImageAsBytes(String imageUrl) {
        try {
            return restClient.get()
                    .uri(imageUrl)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException e) {
            log.warn("이미지 다운로드 실패: url={}", imageUrl, e);
            return null;
        }
    }
}