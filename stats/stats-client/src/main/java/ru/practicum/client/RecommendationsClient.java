package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.controller.RecommendationsControllerGrpc;
import ru.practicum.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.grpc.stats.event.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class RecommendationsClient {

    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    public RecommendationsClient(@GrpcClient("analyzer") RecommendationsControllerGrpc
            .RecommendationsControllerBlockingStub analyzerClient) {
        this.analyzerClient = analyzerClient;
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        log.info("prepare SimilarEventsRequestProto: {}", request);
        Iterator<RecommendedEventProto> iterator = analyzerClient.getSimilarEvents(request);

        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIdList) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIdList)
                .build();
        log.info("prepare InteractionsCountRequestProto: {}", request);
        Iterator<RecommendedEventProto> iterator = analyzerClient.getInteractionsCount(request);
        return asStream(iterator);
    }

    public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        log.info("prepare UserPredictionsRequestProto: {}", request);
        Iterator<RecommendedEventProto> iterator = analyzerClient.getRecommendationsForUser(request);
        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
