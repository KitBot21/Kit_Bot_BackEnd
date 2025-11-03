package com.kit.kitbot.document;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "queries")
public class Query {
    @Id
    private String id;
    private String question;
    private String lang;
    
    public Query(String question, String lang) {
        this.question = question;
        this.lang = lang;

    }
}
