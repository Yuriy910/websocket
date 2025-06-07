package webgroup.websocket.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceService {

    private final SimpUserRegistry simpUserRegistry;

    public boolean isUserOnline(Long userId) {
        boolean isOnline = simpUserRegistry.getUsers().stream()
                .anyMatch(user -> user.getName().equals(String.valueOf(userId)));

        log.info("Проверка онлайн-статуса: userId={}, online={}", userId, isOnline);
        return isOnline;
    }
}


