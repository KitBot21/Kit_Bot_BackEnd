package com.kit.kitbot.config;

import com.kit.kitbot.security.JwtAuthenticationFilter; // [중요] 필터 import
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // [중요] 위치 지정용
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // [추가] 필터 주입을 위해 필요
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // [추가] 만들어둔 경비원 데려오기

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. 누구나 접근 가능한 곳 (로그인, 회원가입, 에러, 스웨거)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/username/check").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        // 1-1. 실시간 인기 질문 키워드 (개발용 hit + 조회)
                        .requestMatchers("/api/popular/answer-keywords").permitAll()
                        .requestMatchers("/api/popular/answer-keywords/*/latest-question").permitAll()
                        .requestMatchers("/api/popular/answer-keywords/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/popular/hit").permitAll()  // 🔹 개발용


                        // 2. 채팅 (오타 수정됨: caht -> chat)
                        .requestMatchers("/chat/**").permitAll()
                        .requestMatchers("/api/crawler/**").permitAll()

                        // 3. 관리자 전용 API
                        .requestMatchers("/api/admin/**").hasAuthority("admin")

                        // 👇 [핵심 변경] 게시판 권한 분리
                        // (1) 조회(GET)는 "로그인한 누구나" (guest 포함) 가능
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").authenticated()

                        // (2) 작성(POST), 수정(PUT), 삭제(DELETE)는 "kumoh" 또는 "admin" 권한만 가능
                        // 주의: User Enum이 소문자(guest, kumoh)이므로 권한 이름도 소문자로 적어야 함
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").hasAnyAuthority("kumoh", "admin")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasAnyAuthority("kumoh", "admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAnyAuthority("kumoh", "admin")

                        // 3. 그 외 나머지 모든 요청은 로그인만 되어 있으면 됨
                        .anyRequest().authenticated()
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 개발 시에는 보통 특정 도메인이나 localhost만 허용하는 게 정석이지만
        // 지금은 개발 단계니 "*"도 괜찮습니다. 나중에 배포할 때 수정하세요.
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}