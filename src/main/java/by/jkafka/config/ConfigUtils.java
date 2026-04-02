package by.jkafka.config;

import by.jkafka.utils.StringUtils;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigUtils {
    public static final String CONFIG_DIR = "config";
    public static final Path CONFIG_ROOT_PATH = getConfigPath();

    @SneakyThrows
    private static Path getConfigPath() {
        var path = Path.of("./" + CONFIG_DIR);
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        return path;
    }

    public static String toStr(Map<String, String> config) {
        return StringUtils.toStr(config);
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
        return StringUtils.parseKeyValues(source);
    }

    public static Map<String, String> parseUiConfig(String source) {
        return parseConfig(source);
    }
}
