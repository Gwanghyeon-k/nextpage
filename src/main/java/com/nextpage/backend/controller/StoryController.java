package com.nextpage.backend.controller;

import com.nextpage.backend.dto.request.StorySaveRequest;
import com.nextpage.backend.dto.response.RootResponseDTO;
import com.nextpage.backend.dto.response.ScenarioResponseDTO;
import com.nextpage.backend.dto.response.StoryDetailsResponseDTO;
import com.nextpage.backend.dto.response.StoryListResponseDTO;
import com.nextpage.backend.result.ResultResponse;
import com.nextpage.backend.service.OpenAiService;
import com.nextpage.backend.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.nextpage.backend.result.ResultCode.*;

@Slf4j
@Tag(name = "Stories", description = "Story 관리")
@RestController
@RequestMapping("/api/v2/stories") // 공통 api
public class StoryController {

    private final StoryService storyService;
    private final OpenAiService openAiService;

    public StoryController(StoryService storyService, OpenAiService openAiService) {
        this.storyService = storyService;
        this.openAiService = openAiService;
    }

    @Operation(summary = "루트 스토리 조회", description = "루트 스토리의 목록을 조회합니다.")
    @GetMapping // 루트 스토리 조회
    public ResponseEntity<ResultResponse> getRootStories() {
        List<RootResponseDTO> rootStoriesList = storyService.getRootStories();
        return ResponseEntity.ok(ResultResponse.of(STORY_LIST_SUCCESS, rootStoriesList));
    }

    @Operation(summary = "스토리 상세 조회", description = "단일 스토리의 상세 내용을 조회합니다.")
    @Parameter(name = "storyId", description = "조회할 스토리 아이디")
    @GetMapping("/details/{storyId}") // 스토리 상세 조회
    public ResponseEntity<ResultResponse> getStoryDetails(@PathVariable Long storyId) {
        StoryDetailsResponseDTO storyDetails = storyService.getStoryDetails(storyId);
        return ResponseEntity.ok(ResultResponse.of(STORY_DETAIL_INFO_SUCCESS, storyDetails));
    }

    @Operation(summary = "스토리 생성", description = "새로운 스토리를 생성합니다.")
    @PostMapping()
    public ResponseEntity<ResultResponse> createStory(@RequestBody @Valid StorySaveRequest storyRequest,
                                                   HttpServletRequest request) {
        log.info("StorySaveRequest: {}", storyRequest);
        log.info("parentId: {}", storyRequest.getParentId());

        storyService.generateStory(storyRequest, request);
        return ResponseEntity.ok(ResultResponse.of(STORY_CREATE_SUCCESS));
    }

    @Operation(summary = "시나리오 조회", description = "시나리오의 스토리 목록을 조회합니다.")
    @Parameter(name = "rootId", description = "조회할 시나리오의 루트 스토리 아이디")
    @GetMapping("/{rootId}") // 시나리오 조회
    public ResponseEntity<ResultResponse> getStoriesByRootId(@PathVariable Long rootId) {
        List<ScenarioResponseDTO> storiesByRoot = storyService.getStoriesByRootId(rootId);
        return ResponseEntity.ok(ResultResponse.of(STORY_LIST_SUCCESS, storiesByRoot));
    }

    @Operation(summary = "특정 분기 조회", description = "특정 분기의 스토리들을 조회합니다.")
    @Parameter(name = "storyId", description = "조회할 분기의 리프 스토리 아이디")
    @GetMapping("/branch/{storyId}") // 특정 분기 조회
    public ResponseEntity<ResultResponse> getStoriesByleafId(@PathVariable Long storyId) {
        List<StoryListResponseDTO> storiesByLeaf = storyService.getStoriesByleafId(storyId);
        return ResponseEntity.ok(ResultResponse.of(STORY_LIST_SUCCESS, storiesByLeaf));
    }

    @Operation(summary = "이미지 생성", description = "스토리의 관련된 이미지를 생성합니다.")
    @PostMapping("/images")
    public ResponseEntity<ResultResponse> generateImage(@RequestBody Map<String, Object> payload) {
        String content = (String) payload.get("content");
        String imageUrl = openAiService.generateImage(content);
        return ResponseEntity.ok(ResultResponse.of(STORY_IMAGE_CREATE_SUCCESS, imageUrl));
    }
}
