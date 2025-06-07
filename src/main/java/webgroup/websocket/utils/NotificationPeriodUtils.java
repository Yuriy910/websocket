package webgroup.websocket.utils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import webgroup.websocket.entities.User;

@Component
public class NotificationPeriodUtils {
    private static final Logger log = LoggerFactory.getLogger(NotificationPeriodUtils.class);

    public boolean isWithinNotificationPeriod(User user, LocalDateTime dateTime) {
        DayOfWeek currentDay = dateTime.getDayOfWeek();
        LocalTime currentTime = dateTime.toLocalTime();

        log.debug("Проверка периода уведомлений для пользователя {}: {} {}", user.getFullName(), currentDay, currentTime);

        return user.getNotificationPeriods().stream().anyMatch(period -> {
            DayOfWeek periodDay = period.getDay();
            LocalTime start = period.getStartTime();
            LocalTime end = period.getEndTime();
            if (periodDay == null || start == null || end == null) {
                log.warn("Пропущен период из-за null-значений: day={}, start={}, end={}", periodDay, start, end);
                return false;
            }

            log.debug("Период: {} ({} - {})", periodDay, start, end);
            return isTimeInNotificationPeriod(currentDay, currentTime, periodDay, start, end);
        });
    }

    private boolean isTimeInNotificationPeriod(DayOfWeek currentDay, LocalTime currentTime, DayOfWeek periodDay, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            return periodDay == currentDay && !currentTime.isBefore(start) && !currentTime.isAfter(end);
        } else {
            if (periodDay == currentDay) {
                return !currentTime.isBefore(start);
            }
            if (nextDay(periodDay) == currentDay) {
                return currentTime.isBefore(end);
            }
        }
        return false;
    }

    private DayOfWeek nextDay(DayOfWeek day) {
        return day == DayOfWeek.SUNDAY ? DayOfWeek.MONDAY : DayOfWeek.of(day.getValue() + 1);
    }
}
