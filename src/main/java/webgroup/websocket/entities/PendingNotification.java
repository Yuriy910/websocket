package webgroup.websocket.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "pending_notification", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "eventId"})
})
public class PendingNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long eventId;

    private LocalDateTime scheduledTime;
}

