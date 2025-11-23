package com.kit.kitbot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // 도구(감별기) 주입

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청(Request)에서 토큰만 쏙 꺼내옵니다.
        String token = resolveToken(request);

        // 2. 토큰이 있고 && 그 토큰이 유효한지(위조 안 됐는지) 검사합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 통과! 토큰에서 "이 사람 누구다(Authentication)" 정보를 가져옵니다.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // 4. 스프링 시큐리티에게 "이 사람 들어왔어! (Context에 저장)"라고 알립니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Security Context에 '{}' 인증 정보를 저장했습니다", authentication.getName());
        }

        // 5. 다음 단계(Controller 등)로 넘어가라!
        filterChain.doFilter(request, response);
    }

    // 헤더에서 "Bearer " 떼고 순수 토큰만 꺼내는 헬퍼 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}