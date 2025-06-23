package com.nextpage.backend.error.exception.bookmark;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class BookmarkNotFoundException extends BusinessException {
    public BookmarkNotFoundException() {
        super(ErrorCode.BOOKMARK_NOT_FOUND);
    }

}
