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
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverBookClient {
    private final NaverBookProperties naverBookProperties;
    private final RestTemplate restTemplate;

    public NaverBookResponse searchByIsbn(String isbn) {
        // 네이버 API가 요구하는 인증용 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverBookProperties.getClientId());
        headers.set("X-Naver-Client-Secret", naverBookProperties.getClientSecret());

        // 헤더를 담은 HTTP 요청 본체(본문 없음)
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 네이버 API 주소에 쿼리 파라미터 붙이기
        String url = naverBookProperties.getBookSearchUrl() + "?d_isbn=" + isbn;

        try {
            // 외부 서버로 HTTP 요청 보내기
            ResponseEntity<NaverBookResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverBookResponse.class
            );
            log.info("네이버 API 도서 검색 완료: isbn={}", isbn);
            return response.getBody();
        } catch (Exception e) {
            log.error("네이버 API 도서 검색 실패: isbn={}", isbn, e);
            throw new BusinessException(ErrorCode.NAVER_API_ERROR, "isbn=" + isbn);
        }
    }
}