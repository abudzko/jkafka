package by.jkafka.ui.connection.panes.topics.read;

import by.jkafka.ui.connection.panes.log.Logger;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;

public class ConsumerRecordService {
    public void saveRecord(ConsumerRecord<String, Object> record) {
        Logger.LOGGER.log(String.format(
                "Received message: Topic: %s, key size = %s, value size= %s, partition = %d, offset = %d",
                record.topic(),
                record.key().length(),
                record.value().toString().getBytes(StandardCharsets.UTF_8).length,
                record.partition(),
                record.offset()
        ));
    }
}
