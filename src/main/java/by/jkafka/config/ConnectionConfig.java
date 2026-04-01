package by.jkafka.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class ConnectionConfig {
    private final String connectionId;
    private Map<String, String> kafkaConfig = new ConcurrentHashMap<>();
    private Map<String, String> uiConfig = new ConcurrentHashMap<>();

    public ConnectionConfig(String connectionId) {
        this.connectionId = connectionId;
    }

    public Map<String, String> getKafkaConfig() {
        Objects.requireNonNull(kafkaConfig);
        return kafkaConfig;
    }

    public Properties properties() {
        Objects.requireNonNull(kafkaConfig);
        var props = new Properties();
        props.putAll(kafkaConfig);
        return props;
    }
}
