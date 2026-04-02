package by.jkafka.utils;


import java.util.LinkedHashMap;
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
}
