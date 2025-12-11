package com.kit.kitbot.service;

import com.kit.kitbot.document.NoticeKeyword;

public interface PushSender {


    boolean sendNoticeKeywordPush(String userId, NoticeKeyword keyword, String noticeId, String title);
}
