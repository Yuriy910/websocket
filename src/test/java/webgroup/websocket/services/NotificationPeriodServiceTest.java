package webgroup.websocket.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import webgroup.websocket.dto.NotificationPeriodDTO;
import webgroup.websocket.entities.NotificationPeriod;
import webgroup.websocket.entities.User;
import webgroup.websocket.mappers.NotificationPeriodMapper;
import webgroup.websocket.repositories.NotificationPeriodRepository;
import webgroup.websocket.repositories.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class NotificationPeriodServiceTest {

    @Mock
    private NotificationPeriodRepository notificationPeriodRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationPeriodMapper mapper;

    @InjectMocks
    private NotificationPeriodService service;

    // getByUser

    @Test
    void getByUser_shouldThrow_whenUserIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.getByUser(null)
        );
        assertEquals("userId не может быть null", ex.getMessage());
    }

    @Test
    void getByUser_shouldReturnDtos() {
        Long userId = 1L;
        NotificationPeriod entity1 = new NotificationPeriod();
        NotificationPeriod entity2 = new NotificationPeriod();

        NotificationPeriodDTO dto1 = new NotificationPeriodDTO();
        NotificationPeriodDTO dto2 = new NotificationPeriodDTO();

        when(notificationPeriodRepository.findByUserId(userId)).thenReturn(List.of(entity1, entity2));
        when(mapper.toDto(entity1)).thenReturn(dto1);
        when(mapper.toDto(entity2)).thenReturn(dto2);

        List<NotificationPeriodDTO> result = service.getByUser(userId);

        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
    }

    // create

    @Test
    void create_shouldThrow_whenUserIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.create(null, new NotificationPeriodDTO())
        );
        assertEquals("userId и period не могут быть null", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenPeriodDtoIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.create(1L, null)
        );
        assertEquals("userId и period не могут быть null", ex.getMessage());
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                service.create(1L, new NotificationPeriodDTO())
        );
        assertTrue(ex.getMessage().contains("Пользователь не найден с id"));
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        Long userId = 1L;
        NotificationPeriodDTO inputDto = new NotificationPeriodDTO();
        NotificationPeriod entity = new NotificationPeriod();
        NotificationPeriod savedEntity = new NotificationPeriod();
        NotificationPeriodDTO outputDto = new NotificationPeriodDTO();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toEntity(inputDto)).thenReturn(entity);
        when(notificationPeriodRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDto(savedEntity)).thenReturn(outputDto);

        NotificationPeriodDTO result = service.create(userId, inputDto);

        assertEquals(outputDto, result);
        assertEquals(user, entity.getUser());

        verify(userRepository).findById(userId);
        verify(notificationPeriodRepository).save(entity);
        verify(mapper).toDto(savedEntity);
    }

    // update

    @Test
    void update_shouldThrow_whenPeriodIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.update(null, new NotificationPeriodDTO())
        );
        assertEquals("id и обновленные данные не могут быть null", ex.getMessage());
    }

    @Test
    void update_shouldThrow_whenUpdatedDtoIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.update(1L, null)
        );
        assertEquals("id и обновленные данные не могут быть null", ex.getMessage());
    }

    @Test
    void update_shouldThrow_whenPeriodNotFound() {
        when(notificationPeriodRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                service.update(1L, new NotificationPeriodDTO())
        );
        assertTrue(ex.getMessage().contains("Период не найден с id"));
    }

    @Test
    void update_shouldUpdateAndReturnDto() {
        Long periodId = 1L;
        NotificationPeriodDTO updatedDto = new NotificationPeriodDTO();
        updatedDto.setDay(DayOfWeek.MONDAY);
        updatedDto.setStartTime(LocalTime.parse("08:00"));
        updatedDto.setEndTime(LocalTime.parse("18:00"));

        NotificationPeriod existing = mock(NotificationPeriod.class);
        NotificationPeriod saved = new NotificationPeriod();
        NotificationPeriodDTO outputDto = new NotificationPeriodDTO();

        when(notificationPeriodRepository.findById(periodId)).thenReturn(Optional.of(existing));
        when(notificationPeriodRepository.save(existing)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(outputDto);

        NotificationPeriodDTO result = service.update(periodId, updatedDto);

        verify(existing).setDay(DayOfWeek.MONDAY);
        verify(existing).setStartTime(LocalTime.parse("08:00"));
        verify(existing).setEndTime(LocalTime.parse("18:00"));
        verify(notificationPeriodRepository).save(existing);
        verify(mapper).toDto(saved);

        assertEquals(outputDto, result);
    }

    @Test
    void delete_shouldThrow_whenIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.delete(null)
        );
        assertEquals("id не может быть null", ex.getMessage());
    }

    @Test
    void delete_shouldThrow_whenPeriodNotExists() {
        when(notificationPeriodRepository.existsById(1L)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () ->
                service.delete(1L)
        );
        assertTrue(ex.getMessage().contains("Период уведомления не найден с id"));
    }

    @Test
    void delete_shouldDeleteById() {
        when(notificationPeriodRepository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(notificationPeriodRepository).deleteById(1L);
    }
}


