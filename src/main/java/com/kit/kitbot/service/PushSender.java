package com.kit.kitbot.service;

import com.kit.kitbot.document.NoticeKeyword;

public interface PushSender {

    /**
     * @return true = 푸시 성공 / false = 실패(토큰없음 등)
     */
    boolean sendNoticeKeywordPush(String userId, NoticeKeyword keyword, String noticeId, String title);
}
