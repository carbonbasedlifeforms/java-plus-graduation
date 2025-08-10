package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.enums.ActionType;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;
import ru.practicum.service.UserActionService;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final UserActionRepository userActionRepository;

    @Transactional
    @Override
    public void saveUserAction(UserActionAvro userActionAvro) {
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        double weight = ActionType.getWeight(userActionAvro.getActionType().name());
        Instant timestamp = userActionAvro.getTimestamp();

        Optional<UserAction> existingUserAction = userActionRepository
                .findByUserIdAndEventId(userId, eventId);

        if (existingUserAction.isEmpty()) {
            UserAction newUserAction = new UserAction()
                    .setUserId(userId)
                    .setEventId(eventId)
                    .setWeight(weight)
                    .setTimestamp(timestamp);

            log.info("Create new user action: {}", newUserAction);
            userActionRepository.save(newUserAction);
        } else if (existingUserAction.get().getWeight() < weight) {
            existingUserAction.get().setWeight(weight);
            existingUserAction.get().setTimestamp(timestamp);
            log.info("Update existing user action: {}", existingUserAction.get());
        }
    }
}
