package com.kit.kitbot.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Document(collection = "queries")
public class Query {
    @Id
    private String id;
    private String question;
    private String lang;

    private List<String> answerKeywords;
    private LocalDateTime createdAt;

    public Query(String question, String lang) {
        this.question = question;
        this.lang = lang;
    }
    
    public Query(String question, String lang, List<String> answerKeywords) {
        this.question = question;
        this.lang = lang;
        this.answerKeywords = answerKeywords;
        this.createdAt = LocalDateTime.now();

    }
}
