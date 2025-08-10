package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.UserActionService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable{
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final UserActionService userActionService;

    @Value("${kafka.topics.user-action}")
    private String userActionTopic;

    @Value("${kafka.consumer.attempt-timeout}")
    private int attemptTimeoutInMs;

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(userActionTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(attemptTimeoutInMs));
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                        log.info("Processing user action {}", record.value());
                        userActionService.saveUserAction(record.value());
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
