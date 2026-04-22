package com.codeit.team4.deokhugam.naver;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverBookClient {
    private final NaverBookProperties naverBookProperties;
    private final RestTemplate restTemplate;

    public NaverBookResponse searchByIsbn(String isbn) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverBookProperties.getClientId());
        headers.set("X-Naver-Client-secret", naverBookProperties.getClientSecret());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = naverBookProperties.getBookSearchUrl() + "?query=" + isbn + "&d_isbn=" + isbn;

        try {
            ResponseEntity<NaverBookResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverBookResponse.class
            );
            log.info("네이버 API 도서 검색 완료: isbn={}", isbn);
            return response.getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NAVER_API_ERROR, "isbn=" + isbn);
        }
    }

}
