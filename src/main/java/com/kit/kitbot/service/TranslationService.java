package com.kit.kitbot.service;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final Translate translate; // 위에서 만든 설정(Bean)이 주입됨

    public String detectLanguage(String text) {
        try {
            Detection detection = translate.detect(text);
            return detection.getLanguage(); // "en", "ko", "ja" 등을 반환
        } catch (Exception e) {
            e.printStackTrace();
            return "ko"; // 에러나면 기본적으로 한국어로 간주
        }
    }

    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(sourceLang), // 예: "en"
                    Translate.TranslateOption.targetLanguage(targetLang)  // 예: "ko"
            );
            return translation.getTranslatedText();
        } catch (Exception e) {
            // 에러 발생 시 로그 찍고 원본 텍스트 반환 (혹은 예외 던지기)
            e.printStackTrace();
            return text;
        }
    }
}