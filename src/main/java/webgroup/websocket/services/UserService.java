package webgroup.websocket.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webgroup.websocket.entities.User;
import webgroup.websocket.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Полное имя пользователя не может быть null или пустым");
        }

        User user = new User(fullName);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserWithNotifications(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null");
        }

        return userRepository.findByIdWithNotificationPeriods(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден с id: " + userId));
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null");
        }

        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
