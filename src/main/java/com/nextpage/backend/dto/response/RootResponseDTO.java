package com.nextpage.backend.dto.response;

import com.nextpage.backend.entity.Story;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RootResponseDTO {
    private final Long id;
    private final String userNickname;
    private final String content;
    private final String imageUrl;
    private final LocalDateTime createdAt;

    @Builder
    public RootResponseDTO(Long id, String userNickname, String content, String imageUrl) {
        this.id = id;
        this.userNickname = userNickname;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
    }

    public static RootResponseDTO of(Story story) {
        return RootResponseDTO.builder()
                .id(story.getId())
                .userNickname(story.getUserNickname())
                .content(story.getContent())
                .imageUrl(story.getImageUrl())
                .build();
    }

}