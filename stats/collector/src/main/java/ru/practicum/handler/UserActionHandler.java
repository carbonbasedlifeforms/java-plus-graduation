package ru.practicum.handler;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.event.UserActionProto;

public interface UserActionHandler {
    UserActionAvro toAvro(UserActionProto userActionProto);

    void handle(UserActionProto userActionProto);
}
