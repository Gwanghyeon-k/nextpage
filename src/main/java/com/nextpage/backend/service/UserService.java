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
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    public SignUpResponseDTO createUser(UserCreateRequest request) {
        String email = request.getEmail();
        String nickname = request.getNickname();
        if (userRepository.existsByEmail(email)) { // 이미 존재하는 이메일이면 유저 생성 x
            throw new EmailDuplicationException();
        }
        User newUser = User.of(email, nickname);
        newUser = userRepository.save(newUser); // db에 유저 저장 - 회원 가입
        newUser = updateNickname(newUser.getId(), newUser.getNickname()); // 유저 닉네임 뒤에 #{id} 붙이기
        return new SignUpResponseDTO(newUser, tokenService.generateAccessToken(newUser.getId()), tokenService.generateRefreshToken());
    }

    public User updateNickname(Long id, String nickname) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
        user.update(nickname + "#" + user.getId());
        userRepository.save(user);
        return user;
    }

    public void deleteUser(HttpServletRequest request) {
        tokenService.validateAccessToken(request); // 만료 검사
        Long userId = tokenService.getUserIdFromToken(request);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    public UserResponseDTO getUserInfo(HttpServletRequest request){
        tokenService.validateAccessToken(request); // 만료 검사
        Long userId = tokenService.getUserIdFromToken(request);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return new UserResponseDTO(user);
    }
}
