package com.nextpage.backend.error.exception.image;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class ImageUploadException extends BusinessException {
    public ImageUploadException(String 이미지_업로드_실패, Exception e) {
        super(ErrorCode.IMAGE_UPLOAD_ERROR);
    }
}