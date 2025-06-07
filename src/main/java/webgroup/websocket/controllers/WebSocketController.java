package webgroup.websocket.controllers;


import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import webgroup.websocket.entities.AckMessage;
import webgroup.websocket.services.NotificationService;


import java.util.Map;


@Slf4j
@Controller

public class WebSocketController {

    private final NotificationService notificationService;

    public WebSocketController(NotificationService notificationService) {
        this.notificationService = notificationService;

    }

    @MessageMapping("/ack")
    public void handleAck(AckMessage ackMessage) {
        log.info("Подтверждение получено в вебсокет контроллер: userId={}, eventId={}",
                ackMessage.getUserId(), ackMessage.getMessageId());

    }

    @MessageMapping("/ping")
    public void requestPendingNotifications(@Payload Map<String, Long> payload) {
        Long userId = payload.get("userId");
        log.info("Получен запрос на отложенные уведомления от userId={}", userId);
        notificationService.sendPendingToFront(userId);
    }

}