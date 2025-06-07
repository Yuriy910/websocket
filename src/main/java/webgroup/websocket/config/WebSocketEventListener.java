package webgroup.websocket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final NotificationWebSocketSender webSocketSender;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = accessor.getFirstNativeHeader("userId");
        String sessionId = accessor.getSessionId();

        log.debug("📥 Новое WebSocket-соединение: sessionId={}, rawUserId={}", sessionId, userIdStr);

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                webSocketSender.registerSession(sessionId, userId);
                log.info("✅ Пользователь {} подключился по WebSocket (sessionId={})", userId, sessionId);
            } catch (NumberFormatException e) {
                log.warn("⚠️ Ошибка парсинга userId='{}' из заголовка. sessionId={}", userIdStr, sessionId);
            }
        } else {
            log.warn("⚠️ Отсутствует userId в заголовках WebSocket. sessionId={}", sessionId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.debug("📴 Отключение WebSocket-сессии: sessionId={}", sessionId);
        webSocketSender.unregisterSession(sessionId);
        log.info("🔌 Отключен пользователь WebSocket-сессии: sessionId={}", sessionId);
    }
}




