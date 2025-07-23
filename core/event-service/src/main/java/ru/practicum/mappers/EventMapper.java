package ru.practicum.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Event;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    Event getEvent(NewEventDto newEventDto);

    @Mapping(target = "initiator.id", source = "initiatorId")
    EventDto getEventDto(Event event);

//    EventDto getEventDto(Event event, UserShortDto initiator);

    @Mapping(target = "comments", source = "commentDtos")
    EventDto getEventDtoWithComments(Event event, List<CommentDto> commentDtos);

    EventShortDto getEventShortDto(Event event);
}
