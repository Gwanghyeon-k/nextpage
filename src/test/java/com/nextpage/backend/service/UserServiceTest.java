package com.nextpage.backend.service;

import com.nextpage.backend.config.jwt.TokenService;
import com.nextpage.backend.dto.request.UserCreateRequest;
import com.nextpage.backend.dto.response.SignUpResponseDTO;
import com.nextpage.backend.dto.response.UserResponseDTO;
import com.nextpage.backend.entity.User;
import com.nextpage.backend.error.exception.user.EmailDuplicationException;
import com.nextpage.backend.error.exception.user.UserNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "test@nextpage.com", "testUser", LocalDateTime.now(), null, false);
    }

    @DisplayName("유저 생성 -> 성공")
    @Test
    void createUser_success() {
        User newUser = User.builder()
                .id(1L)
                .email("newUser@nextpage.com")
                .nickname("newUser")
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .isDeleted(false)
                .build();
        UserCreateRequest request = UserCreateRequest.builder()
                .email(newUser.getEmail())
                .nickname(newUser.getNickname())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(userRepository.findById(newUser.getId())).thenReturn(Optional.of(newUser));
        when(tokenService.generateAccessToken(anyLong())).thenReturn("test_accessToken");
        when(tokenService.generateRefreshToken()).thenReturn("test_refreshToken");

        SignUpResponseDTO response = userService.createUser(request);

        assertNotNull(response); // null이 아닌지 확인
        assertEquals("test_accessToken", response.getAccessToken()); // 일치하는지 확인
        assertEquals("test_refreshToken", response.getRefreshToken());
        verify(userRepository, times(2)).save(any(User.class)); // userRepository.save()가 두 번 호출되었는지 확인
    }

    @DisplayName("유저 생성 -> 이메일 중복")
    @Test
    void createUser_duplicateEmail() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("duplicate@nextpage.com")
                .nickname("duplicateUser")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true); // 중복 처리

        assertThrows(EmailDuplicationException.class, () -> userService.createUser(request));
    }

    @DisplayName("유저 아이디 업데이트 -> 성공")
    @Test
    void updateUserId_success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User updatedUser = userService.updateNickname(user.getId(), user.getNickname());

        assertNotNull(updatedUser);
        assertEquals("testUser#1", updatedUser.getNickname()); // 정상적으로 변경됐는지 확인
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("유저 아이디 업데이트 -> 유저 존재하지 않음")
    @Test
    void updateUserId_userNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateNickname(user.getId(), user.getNickname()));
    }

    @DisplayName("유저 삭제 -> 성공")
    @Test
    void deleteUser_success() {
        doNothing().when(tokenService).validateAccessToken(request); // 만료 여부 통과 처리
        when(tokenService.getUserIdFromToken(request)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.deleteUser(request);

        verify(userRepository, times(1)).delete(user);
    }

    @DisplayName("유저 삭제 -> 유저 존재하지 않음")
    @Test
    void deleteUser_userNotFound() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(request)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(request));
    }

    @DisplayName("유저 조회 -> 성공")
    @Test
    void getUserInfo_success() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(request)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserInfo(request);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @DisplayName("유저 조회 -> 유저 존재하지 않음")
    @Test
    void getUserInfo_userNotFound() {
        doNothing().when(tokenService).validateAccessToken(request);
        when(tokenService.getUserIdFromToken(request)).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(request));
    }
}