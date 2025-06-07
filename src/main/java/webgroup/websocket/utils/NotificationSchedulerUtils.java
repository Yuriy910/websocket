package webgroup.websocket.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import webgroup.websocket.entities.Event;
import webgroup.websocket.entities.NotificationPeriod;
import webgroup.websocket.entities.PendingNotification;
import webgroup.websocket.entities.User;
import webgroup.websocket.repositories.PendingNotificationRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class NotificationSchedulerUtils {

    private static final Logger log = LoggerFactory.getLogger(NotificationSchedulerUtils.class);
    private final PendingNotificationRepository pendingNotificationRepository;

    public NotificationSchedulerUtils(PendingNotificationRepository pendingNotificationRepository) {
        this.pendingNotificationRepository = pendingNotificationRepository;
    }

    public void schedulePendingNotificationIfNecessary(User user, Event event) {
        boolean alreadyScheduled = pendingNotificationRepository.existsByUserIdAndEventId(user.getId(), event.getId());
        if (alreadyScheduled) {
            log.info("Уведомление уже запланировано для пользователя {} по событию {}", user.getFullName(), event.getId());
            return;
        }

        LocalDateTime nextNotificationTime = getNextNotificationTime(user, event.getOccurredAt());
        if (nextNotificationTime == null) {
            log.warn("Не удалось определить время следующего уведомления для пользователя {}", user.getFullName());
            return;
        }

        PendingNotification pending = new PendingNotification();
        pending.setUserId(user.getId());
        pending.setEventId(event.getId());
        pending.setScheduledTime(nextNotificationTime);
        pendingNotificationRepository.save(pending);
        log.info("Запланировано отложенное уведомление для пользователя {} на {}", user.getFullName(), nextNotificationTime);
    }

    public LocalDateTime getNextNotificationTime(User user, LocalDateTime fromTime) {
        Set<NotificationPeriod> periodsSet = user.getNotificationPeriods();

        if (periodsSet == null || periodsSet.isEmpty()) {
            String userName = user.getFullName();
            log.warn("У пользователя {} нет настроек времени уведомлений", userName);
            return null;
        }

        log.debug("Поиск следующего подходящего периода уведомлений для {} с {}...", user.getFullName(), fromTime);

        List<NotificationPeriod> periods = new CopyOnWriteArrayList<>(periodsSet);
        periods.sort(Comparator
                .comparing((NotificationPeriod p) -> p.getDay().getValue())
                .thenComparing(NotificationPeriod::getStartTime));

        LocalDate today = fromTime.toLocalDate();
        DayOfWeek currentDay = fromTime.getDayOfWeek();
        LocalTime currentTime = fromTime.toLocalTime();

        LocalDateTime closest = null;
        NotificationPeriod fallbackPeriod = null;

        for (NotificationPeriod period : periods) {
            DayOfWeek periodDay = period.getDay();
            LocalTime startTime = period.getStartTime();

            int daysUntil = (periodDay.getValue() - currentDay.getValue() + 7) % 7;
            LocalDate targetDate = today.plusDays(daysUntil);
            LocalDateTime targetDateTime = LocalDateTime.of(targetDate, startTime);

            boolean isToday = daysUntil == 0;
            boolean isLaterToday = isToday && startTime.isAfter(currentTime);
            boolean isFutureDay = daysUntil > 0;

            if (isLaterToday || isFutureDay) {
                if (closest == null || targetDateTime.isBefore(closest)) {
                    closest = targetDateTime;
                    log.debug("Ближайший период: {} {} через {} дней", periodDay, startTime, daysUntil);
                }
            }

            if (fallbackPeriod == null || periodDay.getValue() < fallbackPeriod.getDay().getValue() ||
                    (periodDay == fallbackPeriod.getDay() && startTime.isBefore(fallbackPeriod.getStartTime()))) {
                fallbackPeriod = period;
            }
        }

        if (closest != null) {
            return closest;
        }

        if (fallbackPeriod != null) {
            LocalDate nextDate = today.plusWeeks(1)
                    .with(TemporalAdjusters.nextOrSame(fallbackPeriod.getDay()));
            LocalDateTime fallbackTime = LocalDateTime.of(nextDate, fallbackPeriod.getStartTime());
            log.info("Используется fallback период: {} {}", fallbackPeriod.getDay(), fallbackPeriod.getStartTime());
            return fallbackTime;
        }

        log.error("Не удалось определить следующее время уведомления для пользователя {}", user.getFullName());
        return null;
    }
}
