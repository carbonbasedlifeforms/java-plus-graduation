package ru.practicum.service;

import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.ParticipationRequestDto;

import java.util.Collection;

public interface RequestService {

    ParticipationRequestDto newRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    Collection<ParticipationRequestDto> findAllRequestsByUserId(long userId);

    Collection<ParticipationRequestDto> findAllRequestsByEventId(long userId, long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(long userId,
                                                        long eventId,
                                                        EventRequestStatusUpdateRequest request);
}

