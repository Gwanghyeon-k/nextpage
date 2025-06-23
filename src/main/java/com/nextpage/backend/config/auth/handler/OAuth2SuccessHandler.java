package com.nextpage.backend.config.auth.handler;

import com.nextpage.backend.config.jwt.TokenService;
import com.nextpage.backend.entity.User;
import com.nextpage.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response
            , Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        Optional<User> user = userRepository.findByEmail(email);
        Long userId = null;
        String targetUrl;

        if (user.isPresent()) { // 기존 회원인 경우 액세스, 리프레시 토큰 생성 후 전달
            userId = user.get().getId();
            String accessToken = tokenService.generateAccessToken(userId);
            String refreshToken = tokenService.generateRefreshToken();

            targetUrl = UriComponentsBuilder.fromUriString("http://nextpage-front.vercel.app/oauth2/redirect")
                    .queryParam("a", accessToken).queryParam("r", refreshToken)
                    .build().toUriString();
        } else { // 신규 회원인 경우 회원가입 페이지로 이동
            targetUrl = UriComponentsBuilder.fromUriString("http://nextpage-front.vercel.app/login")
                    .queryParam("e", email)
                    .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
