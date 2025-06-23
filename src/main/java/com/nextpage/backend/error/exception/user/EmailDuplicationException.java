package com.nextpage.backend.error.exception.user;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class EmailDuplicationException extends BusinessException {
    public EmailDuplicationException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }

}
