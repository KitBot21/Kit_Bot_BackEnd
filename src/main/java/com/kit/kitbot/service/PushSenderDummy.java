package com.kit.kitbot.service;

import com.kit.kitbot.document.NoticeKeyword;
import org.springframework.stereotype.Component;

@Component
public class PushSenderDummy implements PushSender {

    @Override
    public boolean sendNoticeKeywordPush(String userId, NoticeKeyword keyword, String noticeId, String title) {
        System.out.println("[DummyPush] userId=" + userId +
                ", keyword=" + keyword +
                ", noticeId=" + noticeId +
                ", title=" + title);

        return true; // 항상 성공 처리
    }
}
