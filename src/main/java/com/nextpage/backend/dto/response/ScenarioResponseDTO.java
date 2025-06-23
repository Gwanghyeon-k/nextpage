package com.nextpage.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ScenarioResponseDTO {
    private Long id;
    private Long parentId;
    private String imageUrl;

}
