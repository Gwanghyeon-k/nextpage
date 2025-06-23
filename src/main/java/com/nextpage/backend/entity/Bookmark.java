package com.nextpage.backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "\"bookmarks\"")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id\"", columnDefinition = "INT")
    private Long id;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "\"userId\"", nullable = false)
    private User user;

    @Column(name = "\"storyId\"", columnDefinition = "INT", nullable = false)
    private Long storyId;

    @Column(name = "\"imageUrl\"", nullable = false)
    private String imageUrl;

    @Column(name = "\"createdAt\"", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"isDeleted\"", nullable = false)
    private boolean isDeleted;

    protected Bookmark() {
    }

    @Builder
    public Bookmark(Long id, User user, Long storyId, String imageUrl, LocalDateTime createdAt, boolean isDeleted) {
        this.id = id;
        this.user = user;
        this.storyId = storyId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    public static Bookmark of(User user, Story story) {
        return Bookmark.builder()
                .user(user)
                .storyId(story.getId())
                .imageUrl(story.getImageUrl())
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }
}
