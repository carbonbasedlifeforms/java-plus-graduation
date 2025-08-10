package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.event.UserPredictionsRequestProto;
import ru.practicum.mapper.RecommendationsMapper;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.RecommendedEventProjection;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.service.RecommendationsService;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationsServiceImpl implements RecommendationsService {
    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public Collection<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<RecommendedEventProjection> result = eventSimilarityRepository
                .getRecommendationsForUser(request.getUserId(), request.getMaxResults());
        log.info("Getting recommendations from db: {}", result);
        return map(result);
    }

    @Override
    public Collection<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<RecommendedEventProjection> result = eventSimilarityRepository
                .getSimilarEvents(request.getEventId(), request.getUserId(), request.getMaxResults());
        log.info("Getting similar events from db: {}", result);
        return map(result);
    }

    @Override
    public Collection<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();
        log.info("getEventIdList: {}", eventIds);

        List<RecommendedEventProjection> result = userActionRepository.getCountOfInteractions(eventIds);
        log.info("Getting interaction count from db: {}", result);

        return map(result);
    }

    private Collection<RecommendedEventProto> map(List<RecommendedEventProjection> events) {
        return events.stream()
                .map(RecommendationsMapper::toProtobuf)
                .toList();
    }
}
