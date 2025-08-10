package ru.practicum.controller.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.service.EventService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public Collection<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                               @RequestParam(required = false) List<Long> categories,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false) String rangeStart,
                                               @RequestParam(required = false) String rangeEnd,
                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(required = false) SortingOptions sortingOptions,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               HttpServletRequest request) {
        log.info("getting events public");
        return eventService.findEventsByFilterPublic(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sortingOptions, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventDto getEventById(@RequestHeader("X-EWM-USER-ID") long userId, @PathVariable long eventId) {
        log.info("getting event {} public", eventId);
        return eventService.findEventPublic(eventId, userId);
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getRecommendations(@RequestHeader("X-EWM-USER-ID") long userId,
                                                  @RequestParam(defaultValue = "10") Integer maxResults) {
        log.info("getting recommendations for user {} and size {}", userId, maxResults);
        return eventService.getRecommendations(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void  likeEvent(@RequestHeader("X-EWM-USER-ID") long userId, @PathVariable long eventId) {
        log.info("user {} likes event {}", userId, eventId);
        eventService.likeEvent(eventId, userId);
    }
}
