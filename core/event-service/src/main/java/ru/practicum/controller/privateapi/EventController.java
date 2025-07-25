package ru.practicum.controller.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.service.EventService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto addEvent(@PathVariable long userId,
                             @RequestBody @Valid NewEventDto newEventDto) {
        log.info("adding new event from user {} with body: {}", userId, newEventDto);
        return eventService.save(userId, newEventDto);
    }

    @GetMapping("/{userId}/events")
    public Collection<EventShortDto> getEvents(@PathVariable long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("getting events for user {}", userId);
        return eventService.findEvents(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventDto getEvent(@PathVariable long userId,
                             @PathVariable long eventId) {
        log.info("getting event {} for user {}", eventId, userId);
        return eventService.findEvent(eventId, userId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEvent(@PathVariable long userId,
                             @PathVariable long eventId,
                             @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("updating event {} for user {} with body {}", eventId, userId, updateEventUserRequest);
        return eventService.updateEvent(eventId, userId, updateEventUserRequest);
    }
}
