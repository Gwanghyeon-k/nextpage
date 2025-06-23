package com.nextpage.backend.dto.request;

import com.nextpage.backend.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
public class StorySaveRequest {

    @Schema(defaultValue = "-1")
    private Long parentId = -1L;
    @NotBlank(message = "imageUrl은 필수입니다.")
    private final String imageUrl;
    @NotBlank(message = "content는 필수입니다.")
    private final String content;

    public Story toEntity(String userNickname, String imageUrl, Story parentId) {
        LocalDateTime now = LocalDateTime.now();
        return new Story(
                content,
                imageUrl,
                now,
                now,
                false,
                userNickname,
                parentId
        );
    }
}
