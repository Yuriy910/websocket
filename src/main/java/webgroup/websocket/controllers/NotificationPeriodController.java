package webgroup.websocket.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webgroup.websocket.dto.NotificationPeriodDTO;
import webgroup.websocket.services.NotificationPeriodService;

import java.util.List;


@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationPeriodController {

    private final NotificationPeriodService notificationPeriodService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationPeriodDTO>> getByUser(@PathVariable Long userId) {
        List<NotificationPeriodDTO> dtos = notificationPeriodService.getByUser(userId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<NotificationPeriodDTO> create(
            @PathVariable Long userId,
            @Valid @RequestBody NotificationPeriodDTO dto
    ) {
        NotificationPeriodDTO savedDto = notificationPeriodService.create(userId, dto);
        return ResponseEntity.ok(savedDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationPeriodDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody NotificationPeriodDTO dto
    ) {
        NotificationPeriodDTO updatedDto = notificationPeriodService.update(id, dto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationPeriodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}



