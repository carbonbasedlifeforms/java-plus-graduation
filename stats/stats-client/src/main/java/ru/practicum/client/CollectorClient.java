package ru.practicum.client;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.stats.controller.UserActionControllerGrpc;
import ru.practicum.grpc.stats.event.ActionTypeProto;
import ru.practicum.grpc.stats.event.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
public class CollectorClient {

    private final UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    public CollectorClient(@GrpcClient("collector") UserActionControllerGrpc
            .UserActionControllerBlockingStub collectorClient) {
        this.collectorClient = collectorClient;
    }

    public void sendAction(long userId, long eventId, String ActionType, Instant instant) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.valueOf(ActionType))
                .setTimestamp(getTimestamp(instant))
                .build();
        log.info("Sending action to collector: {}", request);
        Empty empty = collectorClient.collectUserAction(request);
        log.info("Action sent to collector: {}", empty != null ? "success" : "failed");
    }

    private Timestamp getTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}

