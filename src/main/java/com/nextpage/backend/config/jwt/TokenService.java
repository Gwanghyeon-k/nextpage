package com.nextpage.backend.config.jwt;

import com.nextpage.backend.error.exception.auth.TokenNotExistsException;
import com.nextpage.backend.error.exception.auth.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
public class TokenService {
    private Key secretKey;

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.token.access-expire-length}")
    private Long ACCESS_EXPIRE_LENGTH; // 액세스 토큰의 만료 시간

    @Value("${jwt.token.refresh-expire-length}")
    private Long REFRESH_EXPIRE_LENGTH; // 리프레시 토큰의 만료 시간

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    public String generateAccessToken(Long userId) { // 액세스, 리프레시 토큰 생성 로직 구현
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRE_LENGTH))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken() {
        // refresh에는 별다른 유저 정보가 들어가지 않는다. claims 세팅 하지 않음
        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRE_LENGTH))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String reGenerateAccessToken(HttpServletRequest request) { // 액세스 토큰 재발급
        validateRefreshToken(request); // 만료 검사
        Long id = getUserIdFromToken(request);
        return generateAccessToken(id);
    }

    public String resolveAccessToken(HttpServletRequest request) {
        try {
            String header = request.getHeader("AUTHORIZATION");
            return header.substring("Bearer ".length()); // Bearer 을 제외한 문자열 반환
        } catch (Exception e) {
            throw new TokenNotExistsException();
        }
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        try {
            return request.getHeader("REFRESH-TOKEN");
        } catch (Exception e) {
            throw new TokenNotExistsException();
        }
    }

    public void validateAccessToken(HttpServletRequest request) { // 만료 여부 검사
        try {
            String token = resolveAccessToken(request);
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        }
    }

    public void validateRefreshToken(HttpServletRequest request) {
        try {
            String token = resolveRefreshToken(request);
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        } catch (ExpiredJwtException e) { // 토큰이 만료된 경우
            throw new TokenExpiredException();
        } catch (IllegalArgumentException e) { // 토큰이 비어있거나 형식이 잘못된 경우
            throw new TokenNotExistsException();
        }
    }

    public Long getUserIdFromToken(HttpServletRequest request) { // 토큰에서 userId 정보 꺼내기
        String token = resolveAccessToken(request);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }
}