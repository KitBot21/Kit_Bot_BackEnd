package com.kit.kitbot.service;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final Translate translate;

    public String detectLanguage(String text) {
        try {
            Detection detection = translate.detect(text);
            return detection.getLanguage();
        } catch (Exception e) {
            e.printStackTrace();
            return "ko";
        }
    }

    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(sourceLang),
                    Translate.TranslateOption.targetLanguage(targetLang)
            );
            return translation.getTranslatedText();
        } catch (Exception e) {

            e.printStackTrace();
            return text;
        }
    }
}