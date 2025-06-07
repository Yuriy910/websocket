package webgroup.websocket.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    private String fullName;

    private Set<NotificationPeriodDTO> notificationPeriods;
}

