package ru.practicum.producer;


import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollectorKafkaProducer implements AutoCloseable {
    private final KafkaProducer<String, SpecificRecordBase> producer;

    @Override
    public void close() throws Exception {
        producer.flush();
        producer.close();
    }

    public void send(ProducerRecord<String, SpecificRecordBase> record) {
        if (record != null) {
            producer.send(record);
        }
    }
}
