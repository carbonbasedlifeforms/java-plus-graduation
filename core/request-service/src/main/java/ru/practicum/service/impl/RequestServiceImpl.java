package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dal.RequestRepository;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.ParticipationRequestDto;
import ru.practicum.dto.event.enums.EventState;
import ru.practicum.dto.stats.RequestStatus;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public ParticipationRequestDto newRequest(long userId, long eventId) {
        if (requestRepository.findByUserIdAndEventId(userId, eventId).isEmpty()) {
            checkAndGetUser(userId);
            EventDto event = eventClient.findEventById(eventId)
                    .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
            if (event.getInitiator().getId() == userId)
                throw new ConflictException("Event initiator can't make a request");
            if (event.getState() != EventState.PUBLISHED)
                throw new ConflictException("Event with id = " + eventId + " is not published yet");
            if ((event.getParticipantLimit() != 0) && (event.getParticipantLimit() <= event.getConfirmedRequests()))
                throw new ConflictException("Limit of requests reached on event with id = " + event);
            Request request = new Request();
            request.setRequesterId(userId);
            request.setEventId(eventId);
            request.setCreatedOn(LocalDateTime.now());
            if (event.getParticipantLimit() != 0 && event.getRequestModeration())
                request.setStatus(RequestStatus.PENDING);
            else {
                request.setStatus(RequestStatus.CONFIRMED);
                eventClient.updateConfirmedRequests(eventId, event.getConfirmedRequests() + 1L);
            }
            return RequestMapper.INSTANCE.toParticipationRequestDto(requestRepository.save(request));
        } else
            throw new ConflictException("Request from user with id = " + userId +
                    " on event with id = " + eventId + " already exists");
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id = " + requestId + " not found"));
        if (request.getRequesterId() != userId)
            throw new ConflictException("User with id = " + userId + " is not an initializer of request with id = " + requestId);
        requestRepository.delete(request);
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.INSTANCE.toParticipationRequestDto(request);
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByUserId(long userId) {
        checkAndGetUser(userId);
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByUserId(userId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Override
    public Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId) {
        checkAndGetUser(userId);
        Collection<ParticipationRequestDto> result = new ArrayList<>();
        result = requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList();
        return result;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestsStatus(long userId,
                                                               long eventId,
                                                               EventRequestStatusUpdateRequest request) {
        EventDto event = eventClient.findEventById(eventId)
                .orElseThrow(() -> new NotFoundException("event is not found with id = " + eventId));
        if (!event.getInitiator().getId().equals(userId))
            throw new ValidationException("User with id = %d is not a initiator of event with id = %s"
                    .formatted(userId, eventId));

        Collection<Request> requests = requestRepository.findAllRequestsOnEventByIds(eventId,
                request.getRequestIds());
        int limit = event.getParticipantLimit() - event.getConfirmedRequests().intValue();
        int confirmed = event.getConfirmedRequests().intValue();
        if (limit == 0)
            throw new ConflictException("Limit of participant reached");
        for (Request req : requests) {
            if (!RequestStatus.PENDING.equals(req.getStatus()))
                throw new ConflictException("Status of the request with id = %d is %s"
                        .formatted(req.getId(), req.getStatus()));
            if (RequestStatus.REJECTED.equals(request.getStatus())) {
                req.setStatus(RequestStatus.REJECTED);
            } else if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                req.setStatus(RequestStatus.CONFIRMED);
                confirmed++;
            } else if (limit == 0) {
                req.setStatus(RequestStatus.REJECTED);
            } else {
                req.setStatus(RequestStatus.CONFIRMED);
                limit--;
            }
            requestRepository.save(req);
        }
        if (event.getParticipantLimit() != 0)
            eventClient.updateConfirmedRequests(eventId, (long) event.getParticipantLimit() - limit);
        else
            eventClient.updateConfirmedRequests(eventId, confirmed);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(requestRepository.findAllRequestsOnEventByIdsAndStatus(eventId,
                        RequestStatus.CONFIRMED,
                        request.getRequestIds()).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList());
        result.setRejectedRequests(requestRepository.findAllRequestsOnEventByIdsAndStatus(eventId,
                        RequestStatus.REJECTED,
                        request.getRequestIds()).stream()
                .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                .toList());
        return result;
    }

    private void checkAndGetUser(long userId) {
        userClient.findUserById(userId)
                .orElseThrow(() -> new NotFoundException("User is not found with id = " + userId));
    }
}
