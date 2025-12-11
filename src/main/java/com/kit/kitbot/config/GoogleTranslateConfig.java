package com.kit.kitbot.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class GoogleTranslateConfig {

    @Bean
    public Translate googleTranslate() throws IOException {
        ClassPathResource resource = new ClassPathResource("google-trans-key.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

        return TranslateOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}