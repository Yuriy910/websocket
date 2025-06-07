package webgroup.websocket.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webgroup.websocket.services.UserPresenceService;

@RestController
@RequiredArgsConstructor
public class UserIsOnlineController {

    private final UserPresenceService userPresenceService;

    @GetMapping("/api/user/online")
    public ResponseEntity<Boolean> isUserOnline(@RequestParam Long userId) {
        boolean result = userPresenceService.isUserOnline(userId);
        return ResponseEntity.ok(result);
    }
}

