package ru.practicum.handler.impl;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.UserActionProto;
import ru.practicum.handler.UserActionHandler;
import ru.practicum.producer.CollectorKafkaProducer;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {
    private final CollectorKafkaProducer producer;

    @Value("${kafka.topic}")
    private String topic;

    @Override
    public UserActionAvro toAvro(UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setTimestamp(Instant.ofEpochSecond(
                        userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()
                ))
                .setActionType(ActionTypeAvro.valueOf(userActionProto.getActionType().name()))
                .build();
    }

    @Override
    public void handle(UserActionProto userActionProto) {
        producer.send(new ProducerRecord<>(topic, toAvro(userActionProto)));
    }
}
