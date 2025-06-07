package webgroup.websocket.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import webgroup.websocket.config.NotificationWebSocketSender;
import webgroup.websocket.entities.Event;
import webgroup.websocket.entities.User;


import java.util.*;

@Component
public class NotificationUtils {

    private static final Logger log = LoggerFactory.getLogger(NotificationUtils.class);
    private final NotificationWebSocketSender webSocketSender;


    public NotificationUtils(
            NotificationWebSocketSender webSocketSender) {
        this.webSocketSender = webSocketSender;
    }


    public boolean sendNotification(User user, Event event) {
        String msg = String.format("Новое событие: %s", event.getMessage());
        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("eventId", event.getId());
        messagePayload.put("msg", msg);
        String jsonMsg;
        try {
            jsonMsg = new ObjectMapper().writeValueAsString(messagePayload);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации сообщения для пользователя {} (ID={}): {}",
                    user.getFullName(), user.getId(), e.getMessage());
            return false;
        }
        for (int attempt = 1; attempt <= 3; attempt++) {
            log.info("Попытка {} отправки уведомления пользователю {} (ID={}): {}",
                    attempt, user.getFullName(), user.getId(), msg);

            boolean success = webSocketSender.sendToUser(user.getId(), jsonMsg);
            if (success) {
                log.info("Уведомление успешно отправлено пользователю {} (ID={}) на попытке №{}",
                        user.getFullName(), user.getId(), attempt);
                return true;
            } else {
                log.warn("Попытка {} не удалась для пользователя {} (ID={})", attempt, user.getFullName(), user.getId());
            }
        }


        log.warn("Уведомление для пользователя {} (ID={}) сохранено в pending после 3 неудачных попыток",
                user.getFullName(), user.getId());
        return false;
    }


}
