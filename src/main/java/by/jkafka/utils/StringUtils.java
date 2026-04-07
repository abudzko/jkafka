package by.jkafka.utils;


import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class StringUtils {

    private static final String KEY_VALUE_SEPARATOR = "=";

    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static Map<String, String> parseKeyValues(String source) {
        return Optional.ofNullable(source)
                .stream()
                .flatMap(String::lines)
                .filter(StringUtils::hasLength)
                .map(StringUtils::extractKeyValuePair)
                .filter(Objects::nonNull)
                .map(keyValueStr -> {
                    var key = keyValueStr[0].trim();
                    var value = keyValueStr[1].trim();
                    return Map.entry(key, value);
                }).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new));
    }

    public static String toStr(Map<String, String> keyValues) {
        var stringBuilder = new StringBuilder();
        keyValues.forEach((key, value) -> stringBuilder
                .append(key)
                .append(KEY_VALUE_SEPARATOR)
                .append(value)
                .append(System.lineSeparator()));
        return stringBuilder.toString();
    }


    private static String[] extractKeyValuePair(String line) {
        var idx = line.indexOf(KEY_VALUE_SEPARATOR);
        if (idx > 0) {
            return new String[]{line.substring(0, idx), line.substring(idx + 1)};
        }
        return null;
    }

    public static String buildLog(ConsumerRecord<String, Object> record) {
        var strBuilder = new StringBuilder();
        var headers = parseHeaders(record);
        strBuilder
                .append(DateTimeUtils.now())
                .append(" Topic[")
                .append(record.topic())
                .append("]: Key[")
                .append(record.key())
                .append("], Headers")
                .append(headers)
                .append(", Data[")
                .append(record.value())
                .append("]")
                .append(System.lineSeparator());
        return strBuilder.toString();
    }

    private static List<String> parseHeaders(ConsumerRecord<String, Object> record) {
        return Arrays.stream(record.headers().toArray())
                .map(header -> header.key() + KEY_VALUE_SEPARATOR + new String(header.value(), StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }
}
