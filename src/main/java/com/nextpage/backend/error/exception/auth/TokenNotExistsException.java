package com.nextpage.backend.error.exception.auth;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class TokenNotExistsException extends BusinessException {
    public TokenNotExistsException() {
        super(ErrorCode.TOKEN_ACCESS_NOT_EXISTS);
    }

}
