package webgroup.websocket.mappers;


import webgroup.websocket.dto.UserDTO;
import webgroup.websocket.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {NotificationPeriodMapper.class})
public interface UserMapper {
    UserDTO toDto(User user);

}

