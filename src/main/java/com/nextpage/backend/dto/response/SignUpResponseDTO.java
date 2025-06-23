package com.nextpage.backend.dto.response;

import com.nextpage.backend.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignUpResponseDTO {
    private Long id;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    @Builder
    public SignUpResponseDTO(User user, String accessToken, String refreshToken) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
