package webgroup.websocket.dto;


import lombok.Data;
import jakarta.validation.constraints.NotNull;


import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class NotificationPeriodDTO {

    @NotNull(message = "День недели не может быть null")
    private DayOfWeek day;

    @NotNull(message = "Время начала не может быть null")
    private LocalTime startTime;

    @NotNull(message = "Время окончания не может быть null")
    private LocalTime endTime;

}


