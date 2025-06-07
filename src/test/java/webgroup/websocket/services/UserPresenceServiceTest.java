package webgroup.websocket.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class UserPresenceServiceTest {

    @Mock
    private SimpUserRegistry simpUserRegistry;

    @Mock
    private SimpUser simpUser1;

    @Mock
    private SimpUser simpUser2;

    @InjectMocks
    private UserPresenceService userPresenceService;

    @Test
    void isUserOnline_shouldReturnTrue_whenUserIsPresent() {
        Long userId = 42L;

        lenient().when(simpUser1.getName()).thenReturn("41");
        lenient().when(simpUser2.getName()).thenReturn("42");
        when(simpUserRegistry.getUsers()).thenReturn(Set.of(simpUser1, simpUser2));

        boolean online = userPresenceService.isUserOnline(userId);

        assertTrue(online);
        verify(simpUserRegistry).getUsers();
    }

    @Test
    void isUserOnline_shouldReturnFalse_whenUserNotPresent() {
        Long userId = 99L;

        when(simpUser1.getName()).thenReturn("41");
        when(simpUser2.getName()).thenReturn("42");

        when(simpUserRegistry.getUsers()).thenReturn(Set.of(simpUser1, simpUser2));

        boolean online = userPresenceService.isUserOnline(userId);

        assertFalse(online);
        verify(simpUserRegistry).getUsers();
    }
}

