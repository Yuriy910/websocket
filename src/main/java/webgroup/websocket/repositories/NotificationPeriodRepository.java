package webgroup.websocket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import webgroup.websocket.entities.NotificationPeriod;

import java.util.List;

@Repository
public interface NotificationPeriodRepository extends JpaRepository<NotificationPeriod, Long> {
    List<NotificationPeriod> findByUserId(Long userId);
}

