package com.nextpage.backend.dto.response;

import com.nextpage.backend.entity.Story;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StoryListResponseDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private String nickname;

    public static StoryListResponseDTO of(Story story) {
        return StoryListResponseDTO.builder()
                .id(story.getId())
                .content(story.getContent())
                .imageUrl(story.getImageUrl())
                .nickname(story.getUserNickname())
                .build();
    }

}
