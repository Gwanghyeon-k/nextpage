package com.nextpage.backend.service;

import com.nextpage.backend.config.jwt.TokenService;
import com.nextpage.backend.dto.response.BookmarkResponseDTO;
import com.nextpage.backend.dto.response.StoryListResponseDTO;
import com.nextpage.backend.entity.Bookmark;
import com.nextpage.backend.entity.Story;
import com.nextpage.backend.entity.User;
import com.nextpage.backend.error.exception.bookmark.BookmarkNotFoundException;
import com.nextpage.backend.error.exception.user.UserNotFoundException;
import com.nextpage.backend.repository.BookmarkRepository;
import com.nextpage.backend.repository.StoryRepository;
import com.nextpage.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MypageServiceTest {

    @InjectMocks
    private MypageService mypageService;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    private User user;
    private Story story1;
    private Bookmark bookmark;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("nickname1")
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .isDeleted(false)
                .build();
        story1 = Story.builder()
                .id(1L)
                .content("Content1")
                .imageUrl("imageUrl1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .userNickname("nickname1")
                .parentId(null)
                .build();
        bookmark = Bookmark.of(user, story1);
    }

    @Test
    @DisplayName("내가 작성한 스토리 조회 -> 성공")
    void getStoriesByNickname_성공() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        when(storyRepository.findStoriesByNickname(anyString())).thenReturn(Arrays.asList(story1));

        List<StoryListResponseDTO> storyList = mypageService.getStoriesByNickname(request);

        assertThat(storyList).hasSize(1);
        assertThat(storyList.get(0).getContent()).isEqualTo("Content1");

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(storyRepository, times(1)).findStoriesByNickname(user.getNickname());
    }

    @Test
    @DisplayName("내가 작성한 스토리 조회 -> 존재하지 않는 사용자")
    void getStoriesByNickname_사용자_없음() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        assertThrows(UserNotFoundException.class, () -> mypageService.getStoriesByNickname(request));

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(storyRepository, never()).findStoriesByNickname(user.getNickname());
    }

    @Test
    @DisplayName("북마크 추가 -> 성공")
    void addBookmark_성공() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        when(storyRepository.findById(anyLong())).thenReturn(Optional.ofNullable(story1));

        mypageService.addBookmark(request, story1.getId());

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(1L);
        verify(storyRepository, times(1)).findById(1L);
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 추가 -> 사용자 없음")
    void addBookmark_사용자_없음() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        assertThrows(UserNotFoundException.class, () -> mypageService.addBookmark(request, story1.getId()));

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(storyRepository, never()).findById(story1.getId());
        verify(bookmarkRepository, never()).save(bookmark);
    }

    @Test
    @DisplayName("북마크 조회 -> 성공")
    void getBookmarks_성공() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        when(bookmarkRepository.findByUserId(user.getId())).thenReturn(Arrays.asList(bookmark));

        List<BookmarkResponseDTO> bookmarkList = mypageService.getBookmarks(request);

        assertThat(bookmarkList).hasSize(1);
        assertThat(bookmarkList.get(0).getImageUrl()).isEqualTo("imageUrl1");

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookmarkRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    @DisplayName("북마크 조회 -> 사용자 없음")
    void getBookmarks_사용자_없음() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        assertThrows(UserNotFoundException.class, () -> mypageService.getBookmarks(request));

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookmarkRepository, never()).findByUserId(user.getId());
    }

    @Test
    @DisplayName("북마크 삭제 -> 성공")
    void deleteBookmark_성공() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        when(bookmarkRepository.findByUserIdAndStoryId(user.getId(), story1.getId())).thenReturn(Optional.of(bookmark));

        mypageService.deleteBookmark(request, story1.getId());

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookmarkRepository, times(1)).findByUserIdAndStoryId(user.getId(), story1.getId());
        verify(bookmarkRepository, times(1)).delete(bookmark);
    }

    @Test
    @DisplayName("북마크 삭제 -> 사용자 없음")
    void deleteBookmark_사용자_없음() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(null));

        assertThrows(UserNotFoundException.class, () -> mypageService.deleteBookmark(request, story1.getId()));

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookmarkRepository, never()).findByUserIdAndStoryId(anyLong(), anyLong());
        verify(bookmarkRepository, never()).delete(any(Bookmark.class));
    }

    @Test
    @DisplayName("북마크 삭제 -> 북마크 없음")
    void deleteBookmark_북마크_없음() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(any(HttpServletRequest.class))).thenReturn(user.getId());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        when(bookmarkRepository.findByUserIdAndStoryId(user.getId(), story1.getId())).thenReturn(Optional.ofNullable(null));

        assertThrows(BookmarkNotFoundException.class, () -> mypageService.deleteBookmark(request, story1.getId()));

        verify(tokenService, times(1)).getUserIdFromToken(request);
        verify(userRepository, times(1)).findById(user.getId());
        verify(bookmarkRepository, times(1)).findByUserIdAndStoryId(user.getId(), story1.getId());
        verify(bookmarkRepository, never()).delete(any(Bookmark.class));
    }
}
