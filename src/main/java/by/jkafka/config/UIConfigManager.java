package by.jkafka.config;

import by.jkafka.utils.FileUtils;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static by.jkafka.config.ConfigUtils.CONFIG_PATH;
import static by.jkafka.config.ConfigUtils.toStr;

// TODO don't fail when file is absent
public class UIConfigManager {
    private static final String UI_CONFIG_FILE = "ui-config";

    @SneakyThrows
    public static void saveUIConfig(ConnectionConfig connectionConfig) {
        ConfigUtils.prepareDirs(connectionConfig);
        var connectionConfigPath = CONFIG_PATH.resolve(connectionConfig.getConnectionId());
        FileUtils.save(connectionConfigPath.resolve(UI_CONFIG_FILE), toStr(connectionConfig.getUiConfig()));
    }

    @SneakyThrows
    public static Map<String, String> readConfig(Path path) {
        var configFilePath = path.resolve(UI_CONFIG_FILE);
        return ConfigUtils.parseConfig(Files.readString(configFilePath));
    }
}
