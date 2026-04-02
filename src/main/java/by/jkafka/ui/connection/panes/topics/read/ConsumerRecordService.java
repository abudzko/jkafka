package by.jkafka.ui.connection.panes.topics.read;

import by.jkafka.ui.connection.panes.log.Logger;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConsumerRecordService {
    // TODO save event data in rolling file
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
        logInConsole(record);
    }

    private void logInConsole(ConsumerRecord<String, Object> record) {
        var strBuilder = new StringBuilder();
        var headers = parseHeaders(record);
        strBuilder.append(record.topic())
                .append(":")
                .append(record.key())
                .append(",")
                .append(headers)
                .append(",")
                .append(record.value());
        Logger.LOGGER.console(strBuilder.toString());
    }

    private List<String> parseHeaders(ConsumerRecord<String, Object> record) {
        return Arrays.stream(record.headers().toArray())
                .map(header -> header.key() + "=" + new String(header.value(), StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }
}
