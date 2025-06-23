package com.nextpage.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {
    @NotBlank(message = "email은 필수입니다.")
    private String email;
    @NotBlank(message = "nickname은 필수입니다.")
    private String nickname;
}
