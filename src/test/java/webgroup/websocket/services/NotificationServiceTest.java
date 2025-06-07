package webgroup.websocket.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationWebSocketSender webSocketSender;

    @Mock
    private PendingNotificationRepository pendingNotificationRepository;

    @Mock
    private NotificationSchedulerUtils notificationSchedulerUtils;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private NotificationUtils notificationUtils;

    @Mock
    private NotificationPeriodUtils notificationPeriodUtils;

    @InjectMocks
    private NotificationService notificationService;

    private Event event;
    private User user;

    @BeforeEach
    void setup() {
        event = new Event();
        event.setId(100L);
        event.setMessage("Test event");
        event.setOccurredAt(LocalDateTime.now());

        user = new User();
        user.setId(10L);
        user.setFullName("John Doe");
        user.setEvents(new HashSet<>());
        user.setNotificationPeriods(Collections.emptySet());
    }

    @Test
    void processEvent_sendsNotificationAndSavesUser_whenUserInNotificationPeriodAndSendSuccessful() {
        List<User> users = List.of(user);

        when(userRepository.findAllWithNotificationPeriods()).thenReturn(users);
        when(notificationPeriodUtils.isWithinNotificationPeriod(user, event.getOccurredAt())).thenReturn(true);
        when(notificationUtils.sendNotification(user, event)).thenReturn(true);

        notificationService.processEvent(event);

        verify(userRepository).findAllWithNotificationPeriods();
        verify(notificationPeriodUtils).isWithinNotificationPeriod(user, event.getOccurredAt());
        verify(notificationUtils).sendNotification(user, event);
        verify(userRepository).saveAll(argThat(iterable -> {
            List<User> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list.contains(user);
        }));

        verify(notificationSchedulerUtils, never()).schedulePendingNotificationIfNecessary(any(), any());
    }

    @Test
    void processEvent_schedulesPendingNotification_whenSendNotificationFails() {
        List<User> users = List.of(user);

        when(userRepository.findAllWithNotificationPeriods()).thenReturn(users);
        when(notificationPeriodUtils.isWithinNotificationPeriod(user, event.getOccurredAt())).thenReturn(true);
        when(notificationUtils.sendNotification(user, event)).thenReturn(false);

        notificationService.processEvent(event);

        verify(notificationSchedulerUtils).schedulePendingNotificationIfNecessary(user, event);
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void processEvent_schedulesPendingNotification_whenUserNotInNotificationPeriod() {
        List<User> users = List.of(user);

        when(userRepository.findAllWithNotificationPeriods()).thenReturn(users);
        when(notificationPeriodUtils.isWithinNotificationPeriod(user, event.getOccurredAt())).thenReturn(false);

        notificationService.processEvent(event);

        verify(notificationSchedulerUtils).schedulePendingNotificationIfNecessary(user, event);
        verify(notificationUtils, never()).sendNotification(any(), any());
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void processEvent_doesNotProcessIfUserAlreadyHasEvent() {
        user.addEvent(event);
        List<User> users = List.of(user);

        when(userRepository.findAllWithNotificationPeriods()).thenReturn(users);

        notificationService.processEvent(event);

        verify(notificationUtils, never()).sendNotification(any(), any());
        verify(notificationSchedulerUtils, never()).schedulePendingNotificationIfNecessary(any(), any());
        verify(userRepository, never()).saveAll(any());
    }

    @Test
    void sendPendingToFront_sendsNotificationsAndDeletesPending() {
        Long userId = user.getId();

        PendingNotification pendingNotification = new PendingNotification();
        pendingNotification.setUserId(userId);
        pendingNotification.setEventId(event.getId());
        pendingNotification.setScheduledTime(LocalDateTime.now());

        List<PendingNotification> pendingList = List.of(pendingNotification);

        when(pendingNotificationRepository.findAllByUserId(userId)).thenReturn(pendingList);
        when(userRepository.findByIdWithNotificationPeriods(userId)).thenReturn(Optional.of(user));
        when(notificationPeriodUtils.isWithinNotificationPeriod(eq(user), any(LocalDateTime.class))).thenReturn(true);
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
        doNothing().when(webSocketSender).sendToTopic(anyString(), anyString());

        notificationService.sendPendingToFront(userId);

        verify(webSocketSender).sendToTopic(startsWith("/topic/pending/"), anyString());
        verify(pendingNotificationRepository).deleteAll(pendingList);
    }

    @Test
    void sendPendingToFront_doesNothingWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findByIdWithNotificationPeriods(userId)).thenReturn(Optional.empty());

        notificationService.sendPendingToFront(userId);

        verify(webSocketSender, never()).sendToTopic(anyString(), anyString());
        verify(pendingNotificationRepository, never()).deleteAll(anyList());
    }

    @Test
    void sendPendingToFront_doesNotSendWhenOutsideNotificationPeriod() {
        Long userId = user.getId();
        PendingNotification pendingNotification = new PendingNotification();
        pendingNotification.setUserId(userId);
        pendingNotification.setEventId(event.getId());
        pendingNotification.setScheduledTime(LocalDateTime.now());

        List<PendingNotification> pendingList = List.of(pendingNotification);

        when(pendingNotificationRepository.findAllByUserId(userId)).thenReturn(pendingList);
        when(userRepository.findByIdWithNotificationPeriods(userId)).thenReturn(Optional.of(user));
        when(notificationPeriodUtils.isWithinNotificationPeriod(eq(user), any(LocalDateTime.class))).thenReturn(false);

        notificationService.sendPendingToFront(userId);

        verify(webSocketSender, never()).sendToTopic(anyString(), anyString());
        verify(pendingNotificationRepository, never()).deleteAll(anyList());
    }
}
