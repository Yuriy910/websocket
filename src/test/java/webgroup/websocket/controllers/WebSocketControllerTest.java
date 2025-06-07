package webgroup.websocket.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import webgroup.websocket.entities.AckMessage;
import webgroup.websocket.services.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.*;

class WebSocketControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WebSocketController webSocketController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleAck_shouldLogCorrectly() {
        AckMessage ackMessage = new AckMessage();
        ackMessage.setUserId(42L);
        ackMessage.setMessageId(99L);
        webSocketController.handleAck(ackMessage);
    }

    @Test
    void requestPendingNotifications_shouldCallServiceWithCorrectUserId() {
        Long userId = 123L;
        Map<String, Long> payload = Map.of("userId", userId);
        webSocketController.requestPendingNotifications(payload);
        verify(notificationService, times(1)).sendPendingToFront(userId);
    }

    @Test
    void requestPendingNotifications_shouldHandleMissingUserIdGracefully() {
        Map<String, Long> payload = Map.of();
        webSocketController.requestPendingNotifications(payload);
        verify(notificationService).sendPendingToFront(null);
    }
}
