package webgroup.websocket.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webgroup.websocket.config.NotificationWebSocketSender;
import webgroup.websocket.entities.Event;
import webgroup.websocket.entities.PendingNotification;
import webgroup.websocket.entities.User;
import webgroup.websocket.repositories.EventRepository;
import webgroup.websocket.repositories.PendingNotificationRepository;
import webgroup.websocket.repositories.UserRepository;
import webgroup.websocket.utils.NotificationPeriodUtils;
import webgroup.websocket.utils.NotificationSchedulerUtils;
import webgroup.websocket.utils.NotificationUtils;

import java.time.LocalDateTime;
import java.util.*;

import java.util.concurrent.CopyOnWriteArrayList;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationWebSocketSender webSocketSender;
    private final PendingNotificationRepository pendingNotificationRepository;
    private final NotificationSchedulerUtils notificationSchedulerUtils;
    private final EventRepository eventRepository;
    private final NotificationUtils notificationUtils;
    private final NotificationPeriodUtils notificationPeriodUtils;

    @Transactional
    public void processEvent(Event event) {
        log.info("Начата обработка события ID={} [{}]", event.getId(), event.getMessage());

        List<User> users = new CopyOnWriteArrayList<>(userRepository.findAllWithNotificationPeriods());
        List<User> updatedUsers = new CopyOnWriteArrayList<>();
        for (User user : users) {
            log.debug("Обработка пользователя {} (ID={})", user.getFullName(), user.getId());

            boolean alreadyHasEvent = user.getEvents().stream()
                    .anyMatch(e -> e.getId() != null && e.getId().equals(event.getId()));

            if (alreadyHasEvent) {
                log.info("Пользователь {} уже получил событие {}", user.getFullName(), event.getId());
                continue;
            }

            user.addEvent(event);
            log.debug("Событие {} привязано к пользователю {}", event.getId(), user.getFullName());

            if (notificationPeriodUtils.isWithinNotificationPeriod(user, event.getOccurredAt())) {
                if (notificationUtils.sendNotification(user, event)) {
                    log.info("Уведомление успешно отправлено пользователю: {} (eventId={})", user.getId(), event.getId());
                    updatedUsers.add(user);
                } else {
                    log.warn("Не удалось отправить уведомление пользователю: {} (eventId={}). Планируем отложенное уведомление...", user.getId(), event.getId());
                    notificationSchedulerUtils.schedulePendingNotificationIfNecessary(user, event);
                }

            } else {
                notificationSchedulerUtils.schedulePendingNotificationIfNecessary(user, event);
            }

        }

        if (!updatedUsers.isEmpty()) {
            userRepository.saveAll(updatedUsers);
            log.info("Сохранено {} пользователей с новыми событиями", updatedUsers.size());
        }

        log.info("Завершена обработка события ID={}", event.getId());
    }

    public void sendPendingToFront(Long userId) {
        List<PendingNotification> pendingList = pendingNotificationRepository.findAllByUserId(userId);

        List<Map<String, Object>> payloadList = new ArrayList<>();
        List<PendingNotification> toDelete = new ArrayList<>();
        Optional<User> optionalUser = userRepository.findByIdWithNotificationPeriods(userId);
        if (optionalUser.isEmpty()) {
            log.warn("Пользователь с ID={} не найден", userId);
            return;
        }
        User user = optionalUser.get();

        LocalDateTime now = LocalDateTime.now();

        for (PendingNotification pending : pendingList) {
            if (!notificationPeriodUtils.isWithinNotificationPeriod(user, now)) {
                log.info("Уведомление для userId={} не отправлено — вне времени уведомлений", userId);
                continue;
            }
            eventRepository.findById(pending.getEventId()).ifPresent(event -> {
                Map<String, Object> map = new HashMap<>();
                map.put("eventId", event.getId());
                map.put("msg", event.getMessage());
                map.put("scheduledTime", pending.getScheduledTime().toString());
                payloadList.add(map);
                toDelete.add(pending);
            });
        }

        if (!payloadList.isEmpty()) {
            try {
                String json = new ObjectMapper().writeValueAsString(payloadList);
                webSocketSender.sendToTopic("/topic/pending/" + userId, json);
                log.info("Отложенные уведомления отправлены пользователю с ID={}", userId);

                pendingNotificationRepository.deleteAll(toDelete);
                log.info("🗑 Удалено {} отложенных уведомлений пользователя ID={}", toDelete.size(), userId);

            } catch (JsonProcessingException e) {
                log.error("Ошибка сериализации отложенных уведомлений: {}", e.getMessage());
            }
        } else {
            log.info("У пользователя ID={} нет подходящих уведомлений для отправки", userId);
        }
    }


}






