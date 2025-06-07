package webgroup.websocket.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webgroup.websocket.dto.NotificationPeriodDTO;
import webgroup.websocket.entities.NotificationPeriod;
import webgroup.websocket.entities.User;
import webgroup.websocket.mappers.NotificationPeriodMapper;
import webgroup.websocket.repositories.NotificationPeriodRepository;
import webgroup.websocket.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPeriodService {

    private final NotificationPeriodRepository notificationPeriodRepository;
    private final UserRepository userRepository;
    private final NotificationPeriodMapper mapper;

    @Transactional(readOnly = true)
    public List<NotificationPeriodDTO> getByUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId не может быть null");
        }

        List<NotificationPeriod> periods = notificationPeriodRepository.findByUserId(userId);
        return periods.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    public NotificationPeriodDTO create(Long userId, NotificationPeriodDTO periodDto) {
        if (userId == null || periodDto == null) {
            throw new IllegalArgumentException("userId и period не могут быть null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + userId));

        NotificationPeriod period = mapper.toEntity(periodDto);
        period.setUser(user);
        NotificationPeriod saved = notificationPeriodRepository.save(period);

        return mapper.toDto(saved);
    }

    @Transactional
    public NotificationPeriodDTO update(Long periodId, NotificationPeriodDTO updatedDto) {
        if (periodId == null || updatedDto == null) {
            throw new IllegalArgumentException("id и обновленные данные не могут быть null");
        }

        NotificationPeriod existing = notificationPeriodRepository.findById(periodId)
                .orElseThrow(() -> new EntityNotFoundException("Период не найден с id: " + periodId));

        existing.setDay(updatedDto.getDay());
        existing.setStartTime(updatedDto.getStartTime());
        existing.setEndTime(updatedDto.getEndTime());

        NotificationPeriod saved = notificationPeriodRepository.save(existing);

        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id не может быть null");
        }

        if (!notificationPeriodRepository.existsById(id)) {
            throw new EntityNotFoundException("Период уведомления не найден с id: " + id);
        }

        notificationPeriodRepository.deleteById(id);
    }
}

