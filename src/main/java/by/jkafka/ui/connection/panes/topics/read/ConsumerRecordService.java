package by.jkafka.ui.connection.panes.topics.read;

import by.jkafka.ui.connection.panes.log.Logger;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ConsumerRecordService {
    public void saveRecord(ConsumerRecord<String, Object> record) {
        Logger.LOGGER.log(String.format(
                "Received message: Topic: %s, key size = %s, value size= %s, partition = %d, offset = %d",
                record.topic(),
                Optional.ofNullable(record.key()).map(String::toString).map(String::length).orElse(0),
                Optional.ofNullable(record.value())
                        .map(Object::toString)
                        .map(msg -> msg.getBytes(StandardCharsets.UTF_8))
                        .map(bytes -> bytes.length)
                        .orElse(0),
                record.partition(),
                record.offset()
        ));
    }
}
