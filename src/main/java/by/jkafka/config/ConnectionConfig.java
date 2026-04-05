package by.jkafka.config;

import by.jkafka.config.listener.ConfigChangedListener;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class ConnectionConfig {
    private final String connectionId;
    private final Map<String, CopyOnWriteArrayList<ConfigChangedListener>> uiConfigListener = new ConcurrentHashMap<>();
    private Map<String, String> kafkaConfig = new ConcurrentHashMap<>();
    private Map<String, String> uiConfig = new ConcurrentHashMap<>();

    public ConnectionConfig(String connectionId) {
        this.connectionId = connectionId;
    }

    public void addNewUiConfigListener(String name, ConfigChangedListener listener) {
        var listeners = uiConfigListener.computeIfAbsent(name, (key) -> new CopyOnWriteArrayList<>());
        listeners.add(listener);
    }

    public Map<String, String> getKafkaConfig() {
        Objects.requireNonNull(kafkaConfig);
        return kafkaConfig;
    }

    public void updateUiConfig(Map<String, String> uiConfig) {
        var changedUiProps = collectChangedUiProps(uiConfig);
        this.uiConfig.putAll(uiConfig);
        notifyListenersOnChange(changedUiProps);
    }

    private void notifyListenersOnChange(LinkedList<String> changedUiProps) {
        changedUiProps.forEach(changedPropName -> Optional.ofNullable(uiConfigListener.get(changedPropName))
                .ifPresent(listeners -> {
                    listeners.forEach(ConfigChangedListener::onChanged);
                }));
    }

    private LinkedList<String> collectChangedUiProps(Map<String, String> uiConfig) {
        var diff = new LinkedList<String>();
        uiConfig.forEach((key, value) -> {
            if (!this.uiConfig.get(key).equals(value)) {
                diff.add(key);
            }
        });
        return diff;
    }

    public Properties properties() {
        Objects.requireNonNull(kafkaConfig);
        var props = new Properties();
        props.putAll(kafkaConfig);
        return props;
    }
}
