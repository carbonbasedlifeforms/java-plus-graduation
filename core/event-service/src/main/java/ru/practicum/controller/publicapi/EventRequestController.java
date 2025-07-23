package ru.practicum.controller.publicapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.service.EventService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/events/requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestController {
    @Autowired
    EventService eventService;

    @GetMapping("/{eventId}")
    public Optional<EventDto> findEventById(@PathVariable long eventId) {
        log.info("getting optional event by id {} public", eventId);
        return eventService.findEventById(eventId);
    }

    @PutMapping("/{eventId}/confirmed")
    public void updateConfirmedRequests(@PathVariable long eventId, @RequestBody long confirmedRequests) {
        log.info("updating confirmed requests for event {}", eventId);
        eventService.updateConfirmedRequests(eventId, confirmedRequests);
    }
}
