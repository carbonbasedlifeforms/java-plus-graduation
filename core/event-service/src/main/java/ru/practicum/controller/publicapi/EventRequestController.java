package ru.practicum.controller.publicapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventDto;
import ru.practicum.service.EventService;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/events/requests")
@RequiredArgsConstructor
public class EventRequestController {
    private final EventService eventService;

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<EventDto> findEventById(@PathVariable long eventId) {
        log.info("getting optional event by id {} public", eventId);
        return eventService.findEventById(eventId);
    }

    @PutMapping("/{eventId}/confirmed")
    @ResponseStatus(HttpStatus.OK)
    public void updateConfirmedRequests(@PathVariable long eventId, @RequestBody long confirmedRequests) {
        log.info("updating confirmed requests for event {}", eventId);
        eventService.updateConfirmedRequests(eventId, confirmedRequests);
    }
}
