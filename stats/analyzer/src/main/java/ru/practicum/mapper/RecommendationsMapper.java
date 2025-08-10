package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.grpc.stats.event.RecommendedEventProto;
import ru.practicum.repository.RecommendedEventProjection;

@UtilityClass
public class RecommendationsMapper {
    public RecommendedEventProto toProtobuf(RecommendedEventProjection event) {
        return RecommendedEventProto.newBuilder()
                .setEventId(event.getEventId())
                .setScore(event.getScore())
                .build();
    }
}
