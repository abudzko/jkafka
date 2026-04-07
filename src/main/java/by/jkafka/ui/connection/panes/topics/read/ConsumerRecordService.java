package by.jkafka.ui.connection.panes.topics.read;

import by.jkafka.logs.FileLoggingService;
import by.jkafka.utils.StringUtils;
import by.jkafka.utils.logs.LogEvent;
import by.jkafka.utils.logs.Logger;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ConsumerRecordService {
    public void saveRecord(String connectionId, ConsumerRecord<String, Object> record) {
        var log = String.format(
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
        );
        Logger.LOGGER.log(LogEvent.builder().connectionId(connectionId).log(log).build());
        logInConsole(record);
        FileLoggingService.logInFile(connectionId, record);
    }

    private void logInConsole(ConsumerRecord<String, Object> record) {
        var string = StringUtils.buildLog(record);
        Logger.LOGGER.console(string);
    }
}
