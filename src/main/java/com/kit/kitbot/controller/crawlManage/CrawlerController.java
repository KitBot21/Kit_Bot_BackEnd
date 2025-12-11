package com.kit.kitbot.controller.crawlManage;

import com.kit.kitbot.dto.crawl.CrawlerRequestDTO;
import com.kit.kitbot.service.NoticeKeywordNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final NoticeKeywordNotifyService notifyService;

    @PostMapping("/notification")
    public ResponseEntity<String> receiveNotification(@RequestBody CrawlerRequestDTO request) {
        log.info("크롤링 알림 수신: [{}] {}", request.getKeyword(), request.getTitle());

        notifyService.notifySubscribers(request);

        return ResponseEntity.ok("OK");
    }
}