package com.nextpage.backend.dto.response;

import com.nextpage.backend.entity.Story;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class StoryDetailsResponseDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private String userNickname;
    private Long parentId;
    private List<Long> childId;
    private List<String> childContent;

    public static StoryDetailsResponseDTO of(Story story, Long parentId, List<Long> childIdList, List<String> childContentList) {
        return StoryDetailsResponseDTO.builder()
                .id(story.getId())
                .content(story.getContent())
                .imageUrl(story.getImageUrl())
                .userNickname(story.getUserNickname())
                .parentId(parentId)
                .childId(childIdList)
                .childContent(childContentList)
                .build();
    }
}