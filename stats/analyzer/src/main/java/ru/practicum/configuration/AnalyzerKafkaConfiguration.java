package ru.practicum.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.deserializer.EventSimilarityDeserializer;
import ru.practicum.ewm.deserializer.UserActionAvroDeserializer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

import static org.apache.kafka.clients.CommonClientConfigs.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

@Configuration
public class AnalyzerKafkaConfiguration {
    private KafkaConsumer<String, UserActionAvro> userActionConsumer;
    private KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer;

    @Value("${kafka.bootstrap-servers}")
    private String bootStrapServer;

    @Value("${kafka.group-id.user-action}")
    private String userActionGroupId;

    @Value("${kafka.group-id.event-similarity}")
    private String eventSimilarityGroupId;

    @Bean
    public KafkaConsumer<String, UserActionAvro> getUserActionConsumer() {
        if (userActionConsumer == null) {
            Properties config = new Properties();
            config.put(BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
            config.put(GROUP_ID_CONFIG, userActionGroupId);
            config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            config.put(VALUE_DESERIALIZER_CLASS_CONFIG, UserActionAvroDeserializer.class.getName());
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            return new KafkaConsumer<>(config);
        }
        return userActionConsumer;
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> getEventSimilarityConsumer() {
        if (eventSimilarityConsumer == null) {
            Properties config = new Properties();
            config.put(BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
            config.put(GROUP_ID_CONFIG, eventSimilarityGroupId);
            config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            config.put(VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarityDeserializer.class.getName());
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            return new KafkaConsumer<>(config);
        }
        return eventSimilarityConsumer;
    }
}
