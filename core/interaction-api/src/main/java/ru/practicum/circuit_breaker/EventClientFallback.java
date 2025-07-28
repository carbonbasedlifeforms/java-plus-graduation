package ru.practicum.circuit_breaker;

import org.springframework.stereotype.Component;
import ru.practicum.client.EventClient;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.enums.FallBackUtil;
import ru.practicum.exceptions.ServiceFallBackException;

import java.util.Optional;

@Component
public class EventClientFallback implements EventClient {
    @Override
    public Optional<EventDto> findEventById(long eventId) {
        throw new ServiceFallBackException(FallBackUtil.FALLBACK_MESSAGE.getMessage());
    }

    @Override
    public void updateConfirmedRequests(long eventId, long confirmedRequests) {
        throw new ServiceFallBackException(FallBackUtil.FALLBACK_MESSAGE.getMessage());
    }
}
