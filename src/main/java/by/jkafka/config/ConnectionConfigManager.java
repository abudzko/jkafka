package by.jkafka.config;

import by.jkafka.ui.connection.ClusterConfigTemplate;
import by.jkafka.utils.FileUtils;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static by.jkafka.config.ConfigUtils.CONFIG_ROOT_PATH;
import static by.jkafka.config.ConfigUtils.toStr;

public class ConnectionConfigManager {

    private static final String KAFKA_CONFIG_FILE_NAME = "kafka-config";
    private static final String UI_CONFIG_FILE_NAME = "ui-config";

    public static ConnectionConfig createConnectionConfig(ClusterConfigTemplate connectionConfigTemplate) {
        var connectionId = createConnectionId();
        var resource = FileUtils.readResource(connectionConfigTemplate.getTemplate().getTemplatePath());
        var configMap = ConfigUtils.parseConfig(resource);
        var clusterConfig = new ConnectionConfig(connectionId);
        clusterConfig.setKafkaConfig(configMap);
        return clusterConfig;
    }

    private static String createConnectionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @SneakyThrows
    public static List<ConnectionConfig> readConnectionConfigs() {
        try (var configs = Files.list(CONFIG_ROOT_PATH)) {
            return configs.filter(Files::isDirectory).map(path -> {
                var clusterConfig = new ConnectionConfig(path.getFileName().toString());
                clusterConfig.setKafkaConfig(ConfigUtils.readConfigFileOfEmpty(path.resolve(KAFKA_CONFIG_FILE_NAME)));
                clusterConfig.setUiConfig(ConfigUtils.readConfigFileOfEmpty(path.resolve(UI_CONFIG_FILE_NAME)));
                return clusterConfig;

            }).collect(Collectors.toList());
        }
    }

    @SneakyThrows
    public static void saveConfig(ConnectionConfig connectionConfig) {
        var connectionConfigPath = CONFIG_ROOT_PATH.resolve(connectionConfig.getConnectionId());
        Map.of(
                KAFKA_CONFIG_FILE_NAME, connectionConfig.getKafkaConfig(),
                UI_CONFIG_FILE_NAME, connectionConfig.getUiConfig()
        ).forEach((filePath, configMap) ->
                FileUtils.save(connectionConfigPath.resolve(filePath), toStr(configMap)));

    }
}
