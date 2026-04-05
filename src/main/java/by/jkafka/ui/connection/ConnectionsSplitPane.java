package by.jkafka.ui.connection;

import by.jkafka.config.ConfigConstants;
import by.jkafka.config.ConnectionConfig;
import by.jkafka.config.ConnectionConfigManager;
import by.jkafka.config.listener.ConfigChangedListener;
import by.jkafka.ui.connection.panes.ReadmePane;
import by.jkafka.ui.connection.tabs.ConnectionTab;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It is a {@link SplitPane}
 * On left side: the list of connections
 * On right side: tabs, where each tab is connection details
 */
public class ConnectionsSplitPane extends SplitPane {

    private static final String READ_ME_TAB_ID = "readme";
    private final TreeView<ConnectionTreeItem> connectionTreeView;
    private final TabPane rightSideTabPane;
    private final Map<String, Tab> connectionTabs = new ConcurrentHashMap<>();
    private final Map<String, ConnectionConfig> connnectionConfigMap = new ConcurrentHashMap<>();

    public ConnectionsSplitPane() {
        setDividerPositions(0.2);
        rightSideTabPane = new TabPane();
        connectionTreeView = createConnectionsTreeView();
        ConnectionConfigManager.readConnectionConfigs().forEach(this::createConnectionTab);
        getItems().addAll(connectionTreeView, rightSideTabPane);
        rightSideTabPane.prefWidthProperty().bind(widthProperty());
        rightSideTabPane.prefHeightProperty().bind(heightProperty());
    }

    private TreeView<ConnectionTreeItem> createConnectionsTreeView() {
        var rootConnectionTreeItem = new ConnectionTreeItem();
        rootConnectionTreeItem.setName("Connections");
        var connectionsTreeItems = new TreeItem<>(rootConnectionTreeItem);
        connectionsTreeItems.setExpanded(true);
        var treeView = new TreeView<ConnectionTreeItem>();
        treeView.setOnMouseClicked(event -> {
            var selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.isLeaf()) {
                if (event.getClickCount() == 2) { // Double click
                    var connectionId = selectedItem.getValue().connectionId;
                    Optional.ofNullable(connectionId).ifPresent(id -> {
                        var tab = connectionTabs.get(connectionId);
                        if (isConnectionTabOpen(connectionId)) {
                            rightSideTabPane.getSelectionModel().select(tab);
                        } else {
                            rightSideTabPane.getTabs().add(tab);
                            rightSideTabPane.getSelectionModel().select(tab);
                        }
                    });
                }
            }
        });
        treeView.setRoot(connectionsTreeItems);
        return treeView;
    }

    public void createNewConnectionTab(ClusterConfigTemplate connectionConfigTemplate) {
        var connectionConfig = ConnectionConfigManager.createConnectionConfig(connectionConfigTemplate);
        rightSideTabPane.getTabs().add(createConnectionTab(connectionConfig));
    }

    private boolean isConnectionTabOpen(String connectionId) {
        var tabs = rightSideTabPane.getTabs();
        return tabs.stream().anyMatch(tab -> Objects.equals(tab.getId(), connectionId));
    }

    private Tab createConnectionTab(ConnectionConfig connectionConfig) {
        var connectionId = connectionConfig.getConnectionId();
        connnectionConfigMap.put(connectionId, connectionConfig);

        var connectionTreeItem = createConnectionTreeItem(connectionConfig);
        var treeItem = new TreeItem<>(connectionTreeItem);
        connectionTreeView.getRoot().getChildren().add(treeItem);

        var connectionTab = new ConnectionTab(connectionConfig);
        connectionTabs.put(connectionId, connectionTab);
        return connectionTab;
    }

    private ConnectionTreeItem createConnectionTreeItem(ConnectionConfig connectionConfig) {
        var connectionId = connectionConfig.getConnectionId();
        var nameProp = ConfigConstants.CONNECTION_NAME_PROP;
        var connectionName = connectionConfig.getUiConfig().get(nameProp);
        var connectionTreeItem = new ConnectionTreeItem();
        connectionTreeItem.setConnectionId(connectionId);
        connectionTreeItem.setName(connectionName);
        connectionConfig.addNewUiConfigListener(nameProp, new ConfigChangedListener() {
            @Override
            public void onChanged() {
                Platform.runLater(() -> {
                    connectionTreeItem.setName(connectionConfig.getUiConfig().get(nameProp));
                    connectionTreeView.refresh();
                });
            }
        });
        return connectionTreeItem;
    }

    public Tab getOrCreateReadmeTab() {
        var tabs = rightSideTabPane.getTabs();
        return tabs.stream()
                .filter(tab -> Objects.equals(tab.getId(), READ_ME_TAB_ID))
                .findFirst()
                .orElseGet(() -> {
                    var readmeTab = createReadmeTab();
                    tabs.add(readmeTab);
                    return readmeTab;
                });
    }

    private Tab createReadmeTab() {
        var readmeTab = new Tab(READ_ME_TAB_ID);
        readmeTab.setId(READ_ME_TAB_ID);
        var readmePane = new ReadmePane();
        readmeTab.setContent(readmePane);

        readmePane.prefWidthProperty().bind(rightSideTabPane.widthProperty());
        readmePane.prefHeightProperty().bind(rightSideTabPane.heightProperty());
        return readmeTab;
    }

    @Getter
    @Setter
    private static class ConnectionTreeItem {
        private String connectionId;
        private String name;

        @Override
        public String toString() {
            return Optional.ofNullable(name).orElse(connectionId);
        }
    }
}
