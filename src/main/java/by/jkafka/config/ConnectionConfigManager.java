package by.jkafka.config;

import by.jkafka.ui.connection.ClusterConfigTemplate;
import by.jkafka.utils.FileUtils;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static by.jkafka.config.ConfigUtils.CONFIG_PATH;
import static by.jkafka.config.ConfigUtils.toStr;

public class ConnectionConfigManager {

    private static final String CONFIG_FILE_NAME = "kafka-config";

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
        try (var configs = Files.list(CONFIG_PATH)) {
            return configs.filter(Files::isDirectory).map(path -> {
                try {
                    var configFilePath = path.resolve(CONFIG_FILE_NAME);
                    var clusterConfig = new ConnectionConfig(path.getFileName().toString());
                    clusterConfig.setKafkaConfig(ConfigUtils.parseConfig(Files.readString(configFilePath)));
                    clusterConfig.setUiConfig(UIConfigManager.readConfig(path));
                    return clusterConfig;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
    }

    @SneakyThrows
    public static void saveConfig(ConnectionConfig connectionConfig) {
        ConfigUtils.prepareDirs(connectionConfig);
        var connectionConfigPath = CONFIG_PATH.resolve(connectionConfig.getConnectionId());
        FileUtils.save(connectionConfigPath.resolve(CONFIG_FILE_NAME), toStr(connectionConfig.getKafkaConfig()));
        UIConfigManager.saveUIConfig(connectionConfig);
    }
}
