package com.nextpage.backend.entity;


import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;

@Node
@Getter
public class Story {

    @Id @GeneratedValue
    private Long id;
    private String userNickname;
    private String content;
    private String imageUrl;

    // 해당 노드를 자식으로 가지는 관계 : parent
    @Relationship(type = "PARENT_OF", direction = Relationship.Direction.INCOMING)
    private Story parentId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    public Story() {
    }

    public Story(final String content, final String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted, String userNickname, Story parentId) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.userNickname = userNickname;
        this.parentId = parentId;
    }

    @Builder
    public Story(Long id, String content, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted, String userNickname, Story parentId) {
        this.id = id;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.userNickname = userNickname;
        this.parentId = parentId;
    }
}
