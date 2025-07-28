package ru.practicum.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.Constants;
import ru.practicum.StatsHitDto;
import ru.practicum.StatsViewDto;
import ru.practicum.client.StatsClient;
import ru.practicum.client.UserClient;
import ru.practicum.dal.CommentRepository;
import ru.practicum.dal.EventRepository;
import ru.practicum.dal.LocationRepository;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.event.enums.EventActionStateAdmin;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.event.enums.SortingOptions;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.CommentMapper;
import ru.practicum.mappers.EventMapper;
import ru.practicum.mappers.EventUpdater;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final LocationRepository locationRepository;
    private final UserClient userClient;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final StatsClient statsClient;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public EventDto save(long userId, NewEventDto newEventDto) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (newEventDto.getEventDate() != null && newEventDto.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        checkAndGetUser(userId);

        Event event = EventMapper.INSTANCE.getEvent(newEventDto);

        Location location = locationRepository.save(event.getLocation());

        event.setInitiatorId(userId);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLocation(location);
        log.info("Event {} created", event);
        return EventMapper.INSTANCE.getEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto findEvent(long eventId, long userId) {
        log.info("find event by id {} and user id {}", eventId, userId);
        return EventMapper.INSTANCE.getEventDto(
                eventRepository.findByIdAndUserId(eventId, userId)
                        .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId))
        );
    }

    @Override
    public List<EventShortDto> findEvents(long userId, int from, int size) {
        checkAndGetUser(userId);

        Pageable pageable = PageRequest.of(from, size);
        log.info("find events by user id {}", userId);
        return eventRepository.findByUserId(userId, pageable).stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Transactional
    @Override
    public EventDto updateEvent(long eventId, long userId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        Event baseEvent = eventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));

        if (baseEvent.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot updated published event");
        }

        EventUpdater.INSTANCE.update(baseEvent, updateEventUserRequest);
        log.info("Event {} updated", baseEvent);
        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }

    @Transactional
    @Override
    public EventDto updateEventAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        LocalDateTime validDate = LocalDateTime.now().plusHours(2L);
        if (updateEventAdminRequest.getEventDate() != null && updateEventAdminRequest.getEventDate().isBefore(validDate)) {
            throw new ValidationException("Event date should be after two hours after now");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
        checkAndGetUser(event.getInitiatorId());
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.REJECT_EVENT &&
                    event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot cancel published event");
            }

            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.PUBLISH_EVENT &&
                    event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish event not in status Pending");
            }

            if (updateEventAdminRequest.getStateAction() == EventActionStateAdmin.PUBLISH_EVENT &&
                    event.getEventDate().minusHours(1L).isBefore(LocalDateTime.now())) {
                throw new ConflictException("Cannot publish event less than 1 hour before start");
            }
        }

        EventUpdater.INSTANCE.update(event, updateEventAdminRequest);

        if (event.getState() == EventState.PUBLISHED) {
            event.setPublishedOn(LocalDateTime.now());
            event.setConfirmedRequests(0L);
            event.setViews(0L);
        }
        log.info("Event {} updated by admin", event);
        return EventMapper.INSTANCE.getEventDto(event);
    }

    @Override
    public List<EventDto> findEventsByFilter(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             String rangeStart,
                                             String rangeEnd,
                                             int from,
                                             int size) {
        Pageable pageable = PageRequest.of(from, size);

        LocalDateTime start = null;
        LocalDateTime end = null;

        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, Constants.DATE_TIME_FORMATTER);
        }

        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, Constants.DATE_TIME_FORMATTER);
        }
        log.info("find events by filter {} {} {} {} {} {} {}", users, states, categories, start, end, from, size);
        return eventRepository.findAllByFilter(users, states, categories, start, end, pageable).stream()
                .map(EventMapper.INSTANCE::getEventDto)
                .toList();
    }

    @Override
    public List<EventShortDto> findEventsByFilterPublic(String text, List<Long> categories, Boolean paid,
                                                        String rangeStart, String rangeEnd, Boolean onlyAvailable,
                                                        SortingOptions sortingOptions, int from, int size,
                                                        HttpServletRequest request) {
        Pageable pageable;
        if (sortingOptions != null) {
            String sort = sortingOptions == SortingOptions.EVENT_DATE ? "eventDate" : "views";
            pageable = PageRequest.of(from, size, Sort.by(sort).descending());
        } else {
            pageable = PageRequest.of(from, size);
        }

        LocalDateTime start;
        LocalDateTime end = null;

        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, Constants.DATE_TIME_FORMATTER);
        } else {
            start = LocalDateTime.now();
        }

        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, Constants.DATE_TIME_FORMATTER);
            if (end.isBefore(start)) {
                throw new ValidationException("End is before start");
            }
        }

        sendStats(request);

        List<Event> events = eventRepository.findAllByFilterPublic(text, categories, paid, start, end, onlyAvailable,
                EventState.PUBLISHED, pageable);

        List<String> uris = events.stream()
                .map(x -> "/event/" + x.getId())
                .toList();

        String startStatsDate = events.stream()
                .map(Event::getPublishedOn)
                .min(LocalDateTime::compareTo).get().format(Constants.DATE_TIME_FORMATTER);
        String endStatsDate = LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER);

        List<StatsViewDto> statViews = statsClient.getStats(startStatsDate, endStatsDate, uris, false);
        Map<String, Long> eventViews = statViews.stream()
                .collect(Collectors.toMap(StatsViewDto::getUri, StatsViewDto::getHits));
        eventViews.forEach((uri, hits) -> {
            String[] uriSplit = "/".split(uri);
            long partUri = Long.parseLong(uriSplit[uriSplit.length - 1]);
            events.stream()
                    .filter(x -> x.getId() == partUri)
                    .findFirst()
                    .ifPresent(x -> x.setViews(hits));
        });
        eventRepository.saveAll(events);

        log.info("return result {}", events);
        return events.stream()
                .map(EventMapper.INSTANCE::getEventShortDto)
                .toList();
    }

    @Override
    public EventDto findEventPublic(long eventId, HttpServletRequest request) {
        Event baseEvent = eventRepository.findByIdAndStatus(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("published event is not found with id = " + eventId));
        sendStats(request);
        List<StatsViewDto> views = statsClient.getStats(baseEvent.getPublishedOn()
                        .format(Constants.DATE_TIME_FORMATTER),
                LocalDateTime.now().format(Constants.DATE_TIME_FORMATTER),
                List.of(request.getRequestURI()),
                true);
        log.debug("received from stats client list of StatsViewDto: {}", views);
        baseEvent.setViews(views.get(0).getHits());
        Event event = eventRepository.save(baseEvent);
        checkAndGetUser(event.getInitiatorId());
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        if (!comments.isEmpty()) {
            return EventMapper.INSTANCE.getEventDtoWithComments(baseEvent, commentMapper.toCommentDtoList(comments));
        }
        log.info("Event {} found", event);
        return EventMapper.INSTANCE.getEventDto(baseEvent);
    }

    @Override
    public Optional<EventDto> findEventById(long eventId) {
        log.info("find event by id = {}", eventId);
        return eventRepository.findById(eventId).map(EventMapper.INSTANCE::getEventDto);
    }

    @Override
    @Transactional
    public void updateConfirmedRequests(long eventId, long confirmed) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = %s".formatted(eventId)));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Not possible to update confirmed requests for not published event");
        }
        log.info("update confirmed requests for event {}", event);
        event.setConfirmedRequests(confirmed);
    }

    private void checkAndGetUser(long userId) {
        userClient.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
    }

    private void sendStats(HttpServletRequest request) {
        log.debug("save stats hit, uri = {}", request.getRequestURI());
        log.debug("save stats hit, remoteAddr = {}", request.getRemoteAddr());
        statsClient.hit(StatsHitDto.builder()
                .app("event-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
