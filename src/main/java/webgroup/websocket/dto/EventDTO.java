package webgroup.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventDTO {

    private Long id;

    @NotBlank(message = "Сообщение события не может быть пустым")
    private String message;

    @NotNull(message = "Время события должно быть указано")
    @PastOrPresent(message = "Время события не может быть из будущего")
    private LocalDateTime occurredAt;
}

