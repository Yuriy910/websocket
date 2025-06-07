package webgroup.websocket.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import webgroup.websocket.services.UserPresenceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserIsOnlineControllerTest {

    @Mock
    private UserPresenceService userPresenceService;

    @InjectMocks
    private UserIsOnlineController userIsOnlineController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isUserOnline_shouldReturnTrue_whenUserIsOnline() {
        Long userId = 123L;
        when(userPresenceService.isUserOnline(userId)).thenReturn(true);

        ResponseEntity<Boolean> response = userIsOnlineController.isUserOnline(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(true, response.getBody());
    }

    @Test
    void isUserOnline_shouldReturnFalse_whenUserIsOffline() {
        Long userId = 456L;
        when(userPresenceService.isUserOnline(userId)).thenReturn(false);

        ResponseEntity<Boolean> response = userIsOnlineController.isUserOnline(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(false, response.getBody());
    }
}
