package com.nextpage.backend.error.exception.image;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class ImageUploadException extends BusinessException {
    public ImageUploadException() {
        super(ErrorCode.IMAGE_UPLOAD_ERROR);
    }
}