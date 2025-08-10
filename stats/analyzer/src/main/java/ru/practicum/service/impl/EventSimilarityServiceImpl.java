package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventSimilarity;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.service.EventSimilarityService;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    @Override
    public void saveEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        long eventA = eventSimilarityAvro.getEventA();
        long eventB = eventSimilarityAvro.getEventB();
        double score = eventSimilarityAvro.getScore();
        Instant timestamp = eventSimilarityAvro.getTimestamp();

        Optional<EventSimilarity> existingEventSimilarity = eventSimilarityRepository
                .findByEventAAndEventB(eventA, eventB);
        existingEventSimilarity.ifPresent(event -> {
            event.setScore(score);
            event.setTimestamp(timestamp);
            log.info("Updating eventSimilarity: {}", event);
        });
        EventSimilarity eventSimilarity = new EventSimilarity()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(score)
                .setTimestamp(timestamp);
        log.info("Creating new eventSimilarity: {}", eventSimilarity);
        eventSimilarityRepository.save(eventSimilarity);
    }
}
