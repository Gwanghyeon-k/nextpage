package com.nextpage.backend.dto.response;

import com.nextpage.backend.entity.Bookmark;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookmarkResponseDTO {
    private final Long id;
    private final Long userId;
    private final Long storyId;
    private final String imageUrl;

    public static BookmarkResponseDTO of (Bookmark bookmark) {
        return BookmarkResponseDTO.builder()
                .id(bookmark.getId())
                .userId(bookmark.getId())
                .storyId(bookmark.getStoryId())
                .imageUrl(bookmark.getImageUrl())
                .build();
    }
}
