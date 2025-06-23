package com.nextpage.backend.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Global
    INTERNAL_SERVER_ERROR(500, "G001", "서버 오류"),
    INPUT_INVALID_VALUE(409, "G002", "잘못된 입력"),

    // Auth
    TOKEN_ACCESS_NOT_EXISTS(HttpStatus.UNAUTHORIZED.value(), "A001","토큰을 찾을 수 없음"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "A002","액세스 토큰 만료"),

    // Story
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "S001", "스토리를 찾을 수 없음"),
    IMAGE_CREATE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S002", "이미지 생성 실패"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "U001", "유저를 찾을 수 없음"),
    EMAIL_DUPLICATION(HttpStatus.CONFLICT.value(), "U002", "회원 이메일 중복"),

    // OpenAI
    OPENAI_CLIENT_ERROR(HttpStatus.BAD_REQUEST.value(), "O001", "OpenAI 클라이언트 오류"),
    OPENAI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "O002", "OpenAI 서버 오류"),
    OPENAI_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY.value(), "O003", "OpenAI 응답 오류"),

    // Image
    IMAGE_DOWNLOAD_ERROR(HttpStatus.BAD_REQUEST.value(), "I001", "이미지 다운로드 실패"),
    IMAGE_RESIZE_ERROR(HttpStatus.BAD_REQUEST.value(), "I002", "이미지 리사이징 실패"),
    IMAGE_CONVERSION_ERROR(HttpStatus.BAD_REQUEST.value(), "I003", "Webp 변환 실패"),
    IMAGE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST.value(), "I004", "이미지 S3 업로드 실패"),

    // Bookmark
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "B001", "북마크 목록을 찾을 수 없음");

    ;


    private final int status;
    private final String code;
    private final String message;
}