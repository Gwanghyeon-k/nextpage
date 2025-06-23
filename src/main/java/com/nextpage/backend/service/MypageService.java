package com.nextpage.backend.service;

import com.nextpage.backend.config.jwt.TokenService;
import com.nextpage.backend.dto.response.BookmarkResponseDTO;
import com.nextpage.backend.dto.response.StoryListResponseDTO;
import com.nextpage.backend.entity.Bookmark;
import com.nextpage.backend.entity.Story;
import com.nextpage.backend.entity.User;
import com.nextpage.backend.error.exception.bookmark.BookmarkNotFoundException;
import com.nextpage.backend.error.exception.story.StoryNotFoundException;
import com.nextpage.backend.error.exception.user.UserNotFoundException;
import com.nextpage.backend.repository.BookmarkRepository;
import com.nextpage.backend.repository.StoryRepository;
import com.nextpage.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MypageService {
    private final TokenService tokenService;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    public MypageService(TokenService tokenService, StoryRepository storyRepository, UserRepository userRepository, BookmarkRepository bookmarkRepository) {
        this.tokenService = tokenService;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    public List<StoryListResponseDTO> getStoriesByNickname(HttpServletRequest request) { // 내가 작성한 스토리 조회
        tokenService.validateAccessToken(request); // 만료 검사
        Long userId = tokenService.getUserIdFromToken(request);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        List<Story> stories = storyRepository.findStoriesByNickname(user.getNickname());
        return stories.stream()
                .map(StoryListResponseDTO::of)
                .toList();
    }

    public void addBookmark(HttpServletRequest request, Long storyId) { // 북마크 추가
        tokenService.validateAccessToken(request); // 만료 검사
        Long userId = tokenService.getUserIdFromToken(request);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Story story = storyRepository.findById(storyId).orElseThrow(StoryNotFoundException::new);
        Bookmark bookmark = Bookmark.of(user, story);
        bookmarkRepository.save(bookmark);
    }

    public List<BookmarkResponseDTO> getBookmarks(HttpServletRequest request) { // 북마크 조회
        tokenService.validateAccessToken(request); // 만료 검사
        Long userId = tokenService.getUserIdFromToken(request);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(user.getId());
        return bookmarks.stream()
                .map(BookmarkResponseDTO::of)
                .toList();
    }

    public void deleteBookmark(HttpServletRequest request, Long storyId) { // 북마크 삭제
        tokenService.validateAccessToken(request); // 만료 검사
        Long userId = tokenService.getUserIdFromToken(request);
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Bookmark bookmark = bookmarkRepository.findByUserIdAndStoryId(user.getId(), storyId)
                .orElseThrow(BookmarkNotFoundException::new);
        bookmarkRepository.delete(bookmark);
    }
}
