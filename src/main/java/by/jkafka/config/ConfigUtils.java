package by.jkafka.config;

import by.jkafka.utils.StringUtils;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUtils {
    public static final String CONFIG_DIR = "config";
    public static final Path CONFIG_PATH = Path.of("./" + CONFIG_DIR);
    private static final String KEY_VALUE_SEPARATOR = "=";

    public static String toStr(Map<String, String> config) {
        var stringBuilder = new StringBuilder();
        config.forEach((key, value) -> stringBuilder
                .append(key)
                .append(KEY_VALUE_SEPARATOR)
                .append(value)
                .append(System.lineSeparator()));
        return stringBuilder.toString();
    }

    public static Map<String, String> parseConfig(String source) {
        return source.lines()
                .filter(StringUtils::hasLength)
                .map(line -> extractKeyValuePair(line))
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

    private static String[] extractKeyValuePair(String line) {
        var idx = line.indexOf(KEY_VALUE_SEPARATOR);
        if (idx > 0) {
            return new String[]{line.substring(0, idx), line.substring(idx + 1)};
        }
        return null;
    }

    public static Map<String, String> parseUiConfig(String source) {
        return source.lines()
                .filter(StringUtils::hasLength)
                .map(ConfigUtils::extractKeyValuePair)
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

    @SneakyThrows
    public static void prepareDirs(ConnectionConfig connectionConfig) {
        if (!Files.exists(CONFIG_PATH)) {
            Files.createDirectory(CONFIG_PATH);
        }
        var connectionConfigPath = CONFIG_PATH.resolve(connectionConfig.getConnectionId());
        if (!Files.exists(connectionConfigPath)) {
            Files.createDirectory(connectionConfigPath);
        }
    }
}
