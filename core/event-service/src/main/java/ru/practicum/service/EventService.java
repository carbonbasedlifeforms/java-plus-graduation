package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.enums.SortingOptions;

import java.util.List;
import java.util.Optional;

public interface EventService {
    EventDto save(long userId, NewEventDto newEventDto);

    EventDto findEvent(long eventId, long userId);

    List<EventShortDto> findEvents(long userId, int from, int size);

    EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest);

    EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventDto> findEventsByFilter(List<Long> users,
                                      List<String> states,
                                      List<Long> categories,
                                      String rangeStart,
                                      String rangeEnd,
                                      int from,
                                      int size);

    List<EventShortDto> findEventsByFilterPublic(String text,
                                                 List<Long> categories,
                                                 Boolean paid,
                                                 String rangeStart,
                                                 String rangeEnd,
                                                 Boolean onlyAvailable,
                                                 SortingOptions sortingOptions,
                                                 int from,
                                                 int size,
                                                 HttpServletRequest request);

    EventDto findEventPublic(long eventId, HttpServletRequest request);

    Optional<EventDto> findEventById(long eventId);

    void updateConfirmedRequests(long eventId, long confirmed);
}

