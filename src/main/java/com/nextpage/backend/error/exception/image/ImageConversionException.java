package com.nextpage.backend.error.exception.image;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class ImageConversionException extends BusinessException {
    public ImageConversionException() {
        super(ErrorCode.IMAGE_CONVERSION_ERROR);
    }
}