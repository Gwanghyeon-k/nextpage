package com.nextpage.backend.error.exception.openAI;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class OpenAiServerException extends BusinessException {
    public OpenAiServerException() {
        super(ErrorCode.OPENAI_SERVER_ERROR);
    }
}