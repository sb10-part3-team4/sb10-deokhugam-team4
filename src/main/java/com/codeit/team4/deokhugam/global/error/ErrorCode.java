package com.codeit.team4.deokhugam.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다"),

    // Review
    INVALID_RATING(HttpStatus.BAD_REQUEST, "평점은 1~5 사이여야 합니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이미 리뷰를 작성했습니다"),

    // Book
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "이미 존재하는 ISBN입니다"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 도서가 존재하지 않습니다"),
    
    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
    UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "해당 댓글을 수정/삭제할 권한이 없습니다");

    private final HttpStatus status;
    private final String message;
}
