package webgroup.websocket.mappers;

import org.mapstruct.Mapper;
import webgroup.websocket.dto.EventDTO;
import webgroup.websocket.entities.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    Event toEntity(EventDTO dto);

    EventDTO toDto(Event entity);
}

