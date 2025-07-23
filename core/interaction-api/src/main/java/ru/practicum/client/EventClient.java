package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.circuit_breaker.EventClientFallback;
import ru.practicum.dto.event.EventDto;

import java.util.Optional;

@FeignClient(name = "event-service", path = "events/requests", fallback = EventClientFallback.class)
public interface EventClient {
    @GetMapping("/{eventId}")
    Optional<EventDto> findEventById(@PathVariable long eventId);

    @PutMapping("/{eventId}/confirmed")
    void updateConfirmedRequests(@PathVariable long eventId, @RequestBody long confirmedRequests);
}
