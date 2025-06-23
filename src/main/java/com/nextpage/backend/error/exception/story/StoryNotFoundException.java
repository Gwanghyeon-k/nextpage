package com.nextpage.backend.error.exception.story;

import com.nextpage.backend.error.ErrorCode;
import com.nextpage.backend.error.exception.BusinessException;

public class StoryNotFoundException extends BusinessException {
    public StoryNotFoundException() {
        super(ErrorCode.STORY_NOT_FOUND);
    }
}
