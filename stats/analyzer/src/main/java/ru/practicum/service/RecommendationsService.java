package ru.practicum.service;

import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.event.UserPredictionsRequestProto;

import java.util.Collection;

public interface RecommendationsService {
    Collection<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    Collection<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    Collection<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
