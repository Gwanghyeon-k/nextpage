package com.nextpage.backend.error.exception.openAI;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class OpenAiResponseException extends BusinessException {
    public OpenAiResponseException() {
        super(ErrorCode.OPENAI_INVALID_RESPONSE);
    }
}