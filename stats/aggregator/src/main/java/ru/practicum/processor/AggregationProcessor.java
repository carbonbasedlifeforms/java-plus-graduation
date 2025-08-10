package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.AggregatorService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationProcessor {

    @Value("${kafka.topics.user-actions}")
    private String inTopic;

    @Value("${kafka.topics.events-similarity}")
    private String outTopic;

    @Value("${kafka.consumer.attempt-timeout}")
    private int attemptTimeoutInMs;

    private final KafkaProducer<String, EventSimilarityAvro> producer;
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final AggregatorService aggregatorService;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(inTopic));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(attemptTimeoutInMs));
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    log.info("Handling user action: {}", record);
                    List<EventSimilarityAvro> eventSimilarityAvros = aggregatorService.handle(record.value());
                    if (!eventSimilarityAvros.isEmpty()) {
                        eventSimilarityAvros.forEach(event -> {
                            log.info("Sending similarity event: {}", event);
                            producer.send(new ProducerRecord<>(outTopic, event));
                        });
                    }
                    producer.flush();
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Error while processing events from sensors", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();

            } finally {
                log.info("Closing consumer");
                consumer.close();
                log.info("Closing producer");
                producer.close();
            }
        }
    }
}
