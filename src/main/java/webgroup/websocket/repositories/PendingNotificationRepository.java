package webgroup.websocket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webgroup.websocket.entities.PendingNotification;

import java.util.List;
import java.util.Optional;


@Repository
public interface PendingNotificationRepository extends JpaRepository<PendingNotification, Long> {

    List<PendingNotification> findAllByUserId(Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    Optional<PendingNotification> findByUserIdAndEventId(Long userId, Long eventId);
}

