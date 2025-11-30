package com.kit.kitbot.service;

import io.github.jav.exposerversdk.ExpoPushMessage;
import io.github.jav.exposerversdk.ExpoPushTicket;
import io.github.jav.exposerversdk.PushClient;
import io.github.jav.exposerversdk.PushClientException;
import io.github.jav.exposerversdk.ExpoMessageSound;
import io.github.jav.exposerversdk.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class NotificationService {

    public boolean sendPush(String recipientToken, String title, String body, String url) {

        if (!PushClient.isExponentPushToken(recipientToken)) {
            log.warn("유효하지 않은 토큰입니다: {}", recipientToken);
            return false;
        }

        try {
            PushClient client = new PushClient();

            ExpoPushMessage message = new ExpoPushMessage();
            message.getTo().add(recipientToken);
            message.setTitle(title);
            message.setBody(body);
            message.setSound(new ExpoMessageSound("default"));

            if (url != null && !url.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("url", url);
                message.setData(data);
            }

            List<ExpoPushMessage> messages = new ArrayList<>();
            messages.add(message);

            List<List<ExpoPushMessage>> chunks = client.chunkPushNotifications(messages);

            for (List<ExpoPushMessage> chunk : chunks) {
                CompletableFuture<List<ExpoPushTicket>> future = client.sendPushNotificationsAsync(chunk);
                List<ExpoPushTicket> tickets = future.get();

                log.info("티켓 개수: {}", tickets.size());
                for (ExpoPushTicket ticket : tickets) {
                    log.info("티켓 상태: {}, ID: {}, 메시지: {}",
                            ticket.getStatus(),
                            ticket.getId(),
                            ticket.getMessage());
                }

                if (!tickets.isEmpty()) {
                    ExpoPushTicket ticket = tickets.get(0);

                    if (ticket.getStatus() == Status.OK) {
                        log.info("푸시 알림 발송 성공! 토큰: {}", recipientToken);
                        return true;
                    } else {
                        log.error("푸시 알림 발송 실패 - 상태: {}, 메시지: {}, 상세: {}",
                                ticket.getStatus(),
                                ticket.getMessage(),
                                ticket.getDetails());
                    }
                }
            }
        } catch (PushClientException e) {
            log.error("PushClient 생성 중 에러 발생", e);
        } catch (Exception e) {
            log.error("Expo 서버 통신 중 에러 발생: {}", e.getMessage(), e);
        }

        return false;
    }
}