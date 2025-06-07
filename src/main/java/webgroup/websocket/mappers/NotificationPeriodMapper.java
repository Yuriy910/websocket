package webgroup.websocket.mappers;

import org.mapstruct.Mapper;
import webgroup.websocket.dto.NotificationPeriodDTO;
import webgroup.websocket.entities.NotificationPeriod;

@Mapper(componentModel = "spring")
public interface NotificationPeriodMapper {

    NotificationPeriod toEntity(NotificationPeriodDTO dto);

    NotificationPeriodDTO toDto(NotificationPeriod entity);
}

