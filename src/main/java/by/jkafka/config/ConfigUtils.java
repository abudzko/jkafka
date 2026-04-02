package by.jkafka.config;

import by.jkafka.utils.StringUtils;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigUtils {
    public static final String CONFIG_DIR = "config";
    public static final Path CONFIG_ROOT_PATH = getConfigPath();
    private static final String KEY_VALUE_SEPARATOR = "=";

    @SneakyThrows
    private static Path getConfigPath() {
        var path = Path.of("./" + CONFIG_DIR);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        return path;
    }

    public static String toStr(Map<String, String> config) {
        var stringBuilder = new StringBuilder();
        config.forEach((key, value) -> stringBuilder
                .append(key)
                .append(KEY_VALUE_SEPARATOR)
                .append(value)
                .append(System.lineSeparator()));
        return stringBuilder.toString();
    }

    @SneakyThrows
    public static Map<String, String> readConfigFileOfEmpty(Path path) {
        if (Files.exists(path)) {
            return ConfigUtils.parseConfig(Files.readString(path));
        } else {
            return new HashMap<>();
        }
    }

    public static Map<String, String> parseConfig(String source) {
        return source.lines()
                .filter(StringUtils::hasLength)
                .map(ConfigUtils::extractKeyValuePair)
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

    public static Map<String, String> parseUiConfig(String source) {
        return parseConfig(source);
    }

    private static String[] extractKeyValuePair(String line) {
        var idx = line.indexOf(KEY_VALUE_SEPARATOR);
        if (idx > 0) {
            return new String[]{line.substring(0, idx), line.substring(idx + 1)};
        }
        return null;
    }
}
