package webgroup.websocket.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AckMessage {
    private Long userId;
    private Long messageId;
}
