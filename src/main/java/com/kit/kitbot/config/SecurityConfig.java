package com.kit.kitbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

//
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /* ===== dev 프로파일: MockUserFilter 활성화 & Swagger 허용 ===== */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/chat/**").permitAll()
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .anyRequest().permitAll()  // 테스트를 위해 모두 허용
                );

        return http.build();
    }
}