package com.nextpage.backend.error.exception.image;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class ImageResizeException extends BusinessException {
    public ImageResizeException() {
        super(ErrorCode.IMAGE_RESIZE_ERROR);
    }
}