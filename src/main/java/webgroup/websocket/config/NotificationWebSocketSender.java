package webgroup.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import webgroup.websocket.services.UserPresenceService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketSender {

    private final SimpMessagingTemplate messagingTemplate;
    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final UserPresenceService userPresenceService;

    public void registerSession(String sessionId, Long userId) {
        sessionUserMap.put(sessionId, userId);
        onlineUsers.add(userId);
        log.info("Сессия зарегистрирована: sessionId={}, userId={}. Онлайн: {}", sessionId, userId, onlineUsers.size());
    }

    public void unregisterSession(String sessionId) {
        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            onlineUsers.remove(userId);
            log.info("Сессия удалена: sessionId={}, userId={}. Онлайн: {}", sessionId, userId, onlineUsers.size());
        } else {
            log.warn("Попытка удалить неизвестную сессию: sessionId={}", sessionId);
        }
    }

    public boolean sendToUser(Long userId, String message) {
        if (!userPresenceService.isUserOnline(userId)) {
            log.warn("Пользователь {} не в сети. Сообщение не отправлено: {}", userId, message);
            return false;
        }

        try {
            messagingTemplate.convertAndSend("/topic/notify/" + userId, message);
            log.info("Сообщение отправлено пользователю {}: {}", userId, message);
            return true;
        } catch (Exception e) {
            log.error("Не удалось отправить сообщение пользователю {}. Ошибка: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    public void sendToTopic(String destination, String payload) {
        log.info("Отправка сообщения в топик: {}, payload: {}", destination, payload);
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.info("Сообщение успешно отправлено в топик: {}", destination);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в топик: {}, ошибка: {}", destination, e.getMessage(), e);
        }
    }
}


