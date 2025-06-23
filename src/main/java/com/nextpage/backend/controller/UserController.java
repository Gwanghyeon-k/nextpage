package com.nextpage.backend.controller;

import com.nextpage.backend.config.jwt.TokenService;
import com.nextpage.backend.dto.request.UserCreateRequest;
import com.nextpage.backend.dto.response.SignUpResponseDTO;
import com.nextpage.backend.dto.response.UserResponseDTO;
import com.nextpage.backend.result.ResultResponse;
import com.nextpage.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.nextpage.backend.result.ResultCode.*;

@Tag(name = "Users", description = "User 관리")
@RestController
@RequestMapping("/api/v2/users") // 공통 api
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "회원가입", description = "유저를 생성합니다.")
    @PostMapping
    public ResponseEntity<ResultResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        SignUpResponseDTO user = userService.createUser(request);
        return ResponseEntity.ok(ResultResponse.of(USER_CREATE_SUCCESS, user));
    }

    @Operation(summary = "회원탈퇴", description = "유저를 삭제합니다. (Hard Delete)")
    @DeleteMapping("/delete")
    public ResponseEntity<ResultResponse> deleteUser(HttpServletRequest request) {
        userService.deleteUser(request);
        return ResponseEntity.ok(ResultResponse.of(USER_DELETE_SUCCESS, null));
    }

    @Operation(summary = "액세스 토큰 재발급", description = "리프레쉬 토큰을 검사해 액세스 토큰 재발급 요청을 처리합니다.")
    @PostMapping("/auth/token")
    public ResponseEntity<ResultResponse> reGenerateAccessToken(HttpServletRequest request) {
        // 이 때 받은 토큰은 refresh 토큰
        String accessToken = tokenService.reGenerateAccessToken(request);
        return ResponseEntity.ok(ResultResponse.of(TOKEN_CREATE_SUCCESS, accessToken));
    }

    @Operation(summary = "토큰으로 사용자 조회 API", description = "토큰을 통해 사용자 정보를 조회합니다.")
    @GetMapping("/details") // 토큰으로 사용자 조회
    public ResponseEntity<ResultResponse> getUserByToken(HttpServletRequest request) {
        UserResponseDTO userResponseDTO = userService.getUserInfo(request);
        return ResponseEntity.ok(ResultResponse.of(USER_DETAIL_INFO_SUCCESS, userResponseDTO));
    }
}
