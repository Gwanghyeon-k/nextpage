package com.nextpage.backend.error.exception.image;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class ImageDownloadException extends BusinessException {
    public ImageDownloadException() {
        super(ErrorCode.IMAGE_DOWNLOAD_ERROR);
    }
}