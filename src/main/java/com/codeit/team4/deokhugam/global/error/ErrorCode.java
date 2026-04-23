package com.codeit.team4.deokhugam.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다"),
    MISSING_HEADER(HttpStatus.BAD_REQUEST, "필수 헤더가 누락되었습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 올바르지 않습니다"),
    USER_FORBIDDEN(HttpStatus.FORBIDDEN, "로그인한 사용자 본인만 접근할 수 있습니다"),

    // Review
    INVALID_RATING(HttpStatus.BAD_REQUEST, "평점은 1~5 사이여야 합니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "이미 리뷰를 작성했습니다"),
    REVIEW_NOT_OWNER(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다"),

    // Book
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "이미 존재하는 ISBN입니다"),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 도서가 존재하지 않습니다"),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
    COMMENT_NOT_OWNER(HttpStatus.FORBIDDEN, "해당 댓글을 수정/삭제할 권한이 없습니다"),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

    // Naver API
    NAVER_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 API 호출에 실패했습니다."),

    // S3
    S3_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다."),
    S3_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 삭제에 실패했습니다."),
    S3_EMPTY_FILE_ERROR(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    S3_INVALID_FILE_TYPE_ERROR(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다.");

    private final HttpStatus status;
    private final String message;
}
