package com.nextpage.backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "\"users\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"id\"", columnDefinition = "INT")
    private Long id;

    @Column(name = "\"email\"", unique = true, nullable = false)
    private String email;

    @Column(name = "\"nickname\"", unique = true, nullable = false)
    private String nickname;

    @Column(name = "\"createdAt\"", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "\"updatedAt\"")
    private LocalDateTime updatedAt;

    @Column(name = "\"isDeleted\"", nullable = false)
    private boolean isDeleted;


    public User() {
    }

    @Builder
    public User(Long id, String email, String nickname, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean isDeleted) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
    }

    public static User of(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
    }

    public User update(String nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
        return this;
    }
}
