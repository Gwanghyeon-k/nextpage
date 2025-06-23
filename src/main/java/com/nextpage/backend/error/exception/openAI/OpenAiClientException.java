package com.nextpage.backend.error.exception.openAI;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class OpenAiClientException extends BusinessException {
    public OpenAiClientException() {
        super(ErrorCode.OPENAI_CLIENT_ERROR);
    }
}
