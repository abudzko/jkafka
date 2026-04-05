package by.jkafka.ui.connection.tabs;

import by.jkafka.config.ConfigConstants;
import by.jkafka.config.ConnectionConfig;
import by.jkafka.config.listener.ConfigChangedListener;
import by.jkafka.ui.connection.panes.ConnectionPane;
import javafx.application.Platform;
import javafx.scene.control.Tab;

import java.util.Optional;

public class ConnectionTab extends Tab {

    private final ConnectionConfig connectionConfig;

    public ConnectionTab(ConnectionConfig connectionConfig){
        this.connectionConfig = connectionConfig;
        var nameProp = ConfigConstants.CONNECTION_NAME_PROP;
        var connectionId = connectionConfig.getConnectionId();
        var connectionName = connectionConfig.getUiConfig().get(nameProp);

        var tabName = Optional.ofNullable(connectionName).orElse(connectionId);
        setText(tabName);
        setId(connectionId);
        var newConnectionPane = new ConnectionPane(connectionConfig);
        setContent(newConnectionPane);

        connectionConfig.addNewUiConfigListener(nameProp, new ConfigChangedListener() {
            @Override
            public void onChanged() {
                Platform.runLater(() -> {
                    setText(connectionConfig.getUiConfig().get(nameProp));
                });
            }
        });
    }
}
