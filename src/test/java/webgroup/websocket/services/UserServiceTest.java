package webgroup.websocket.services;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import webgroup.websocket.entities.User;
import webgroup.websocket.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldSaveUser_whenFullNameIsValid() {
        String fullName = "John Doe";
        User user = new User(fullName);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(fullName);

        assertEquals(fullName, result.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenFullNameIsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser("  "));
    }

    @Test
    void getUserWithNotifications_shouldReturnUser_whenUserExists() {
        Long userId = 1L;
        User user = new User("John Doe");
        when(userRepository.findByIdWithNotificationPeriods(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserWithNotifications(userId);

        assertEquals(user, result);
        verify(userRepository).findByIdWithNotificationPeriods(userId);
    }

    @Test
    void getUserWithNotifications_shouldThrowException_whenUserNotFound() {
        Long userId = 1L;
        when(userRepository.findByIdWithNotificationPeriods(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserWithNotifications(userId));
    }

    @Test
    void getUserWithNotifications_shouldThrowException_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserWithNotifications(null));
    }

    @Test
    void deleteUser_shouldDeleteAndReturnTrue_whenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        boolean result = userService.deleteUser(userId);

        assertTrue(result);
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_shouldReturnFalse_whenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        boolean result = userService.deleteUser(userId);

        assertFalse(result);
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_shouldThrowException_whenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(null));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = List.of(new User("John"), new User("Jane"));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(users, result);
        verify(userRepository).findAll();
    }
}

