package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.EventSimilarityService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventsSimilarityProcessor {
    private final KafkaConsumer<String, EventSimilarityAvro> consumer;
    private final EventSimilarityService eventSimilarityService;

    @Value("${kafka.topics.event-similarity}")
    private String eventSimilarityTopic;

    @Value("${kafka.consumer.attempt-timeout}")
    private int attemptTimeoutInMs;

    public void start() {
        try {
            consumer.subscribe(List.of(eventSimilarityTopic));
            log.info("Subscribed to {} topic", eventSimilarityTopic);

            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer
                        .poll(Duration.ofMillis(attemptTimeoutInMs));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    log.info("Processing similarity consumer message {}", record.value());
                    eventSimilarityService.saveEventSimilarity(record.value());
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Error while processing events from hub", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Closing consumer");
                consumer.close();
            }
        }
    }
}
