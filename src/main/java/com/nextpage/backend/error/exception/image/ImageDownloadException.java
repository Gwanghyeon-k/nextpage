package com.nextpage.backend.error.exception.image;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class ImageDownloadException extends BusinessException {
    public ImageDownloadException(String s) {
        super(ErrorCode.IMAGE_DOWNLOAD_ERROR);
    }
}