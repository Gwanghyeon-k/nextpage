package com.nextpage.backend.service;

import com.nextpage.backend.config.jwt.TokenService;
import com.nextpage.backend.dto.request.StorySaveRequest;
import com.nextpage.backend.dto.response.RootResponseDTO;
import com.nextpage.backend.dto.response.ScenarioResponseDTO;
import com.nextpage.backend.dto.response.StoryDetailsResponseDTO;
import com.nextpage.backend.dto.response.StoryListResponseDTO;
import com.nextpage.backend.entity.Story;
import com.nextpage.backend.error.exception.image.ImageDownloadException;
import com.nextpage.backend.error.exception.image.ImageUploadException;
import com.nextpage.backend.error.exception.story.StoryNotFoundException;
import com.nextpage.backend.error.exception.user.UserNotFoundException;
import com.nextpage.backend.repository.StoryRepository;
import com.nextpage.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoryService {
    private final StoryRepository storyRepository;
    private final ImageService imageService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    // parentId가 없는 루트 스토리 목록 조회
    public List<RootResponseDTO> getRootStories() {
        List<Story> rootStories = storyRepository.findRootStories();
        List<RootResponseDTO> rootStoriesList = rootStories.stream()
                .map(RootResponseDTO::of)
                .toList();
        if (rootStoriesList.isEmpty()) { throw new StoryNotFoundException(); }
        return rootStoriesList;
    }

    public StoryDetailsResponseDTO getStoryDetails(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(StoryNotFoundException::new);
        Long parentId = story.getParentId() != null ? story.getParentId().getId() : null;
        return StoryDetailsResponseDTO.of(story, parentId, getChildIds(story), getChildContents(story));
    }

    public List<Long> getChildIds(Story story) {
        return storyRepository.findChildByParentId(story.getId()).stream().map(Story::getId).toList();
    }

    public List<String> getChildContents(Story story) {
        return storyRepository.findChildByParentId(story.getId()).stream().map(Story::getContent).toList();
    }

    public void generateStory(StorySaveRequest request, HttpServletRequest httpServletRequest) {
        String userNickname = getUserNickname(httpServletRequest);
        String s3Url;
        try {
            s3Url = imageService.uploadWithLambda(request.getImageUrl());
        } catch (ImageDownloadException | ImageUploadException e) {
            log.error("이미지 처리 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
        Story parentStory = getParentById(request.getParentId());
        Story story = request.toEntity(userNickname, s3Url, parentStory);
        storyRepository.save(story);
    }

    private String getUserNickname(HttpServletRequest httpServletRequest) {
        Long userId = tokenService.getUserIdFromToken(httpServletRequest);
        return userRepository.findNicknameById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private Story getParentById(Long parentId) {
        return parentId != null ? storyRepository.findById(parentId).orElse(null) : null;
    }

    public List<ScenarioResponseDTO> getStoriesByRootId(Long rootId) {
        List<Story> result = storyRepository.findAllChildrenByRootId(rootId);
        List<ScenarioResponseDTO> stories = new ArrayList<>();
        for (Story story : result) {
            Long parentId = getParentId(story);
            ScenarioResponseDTO dto = new ScenarioResponseDTO(
                    story.getId(), parentId, story.getImageUrl()
            );
            stories.add(dto);
        }
        if (stories.isEmpty()) { throw new StoryNotFoundException(); }
        return stories;
    }

    public List<StoryListResponseDTO> getStoriesByleafId(Long leafId) {
        List<Story> result = storyRepository.findRecursivelyByLeafId(leafId);
        List<StoryListResponseDTO> stories = new ArrayList<>();
        for (Story story : result) {
            stories.add(StoryListResponseDTO.of(story));
        }
        Collections.reverse(stories);
        if (stories.isEmpty()) { throw new StoryNotFoundException(); }
        return stories;
    }

    public Long getParentId(Story story) {
        Optional<Story> parentOpt = storyRepository.findParentByChildId(story.getId());
        return parentOpt.map(Story::getId).orElse(null);
    }
}
