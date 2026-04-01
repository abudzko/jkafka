package by.jkafka.ui.connection;

import by.jkafka.config.ConnectionConfigManager;
import by.jkafka.config.ConnectionConfig;
import by.jkafka.ui.connection.panes.ConnectionPane;
import by.jkafka.ui.connection.panes.ReadmePane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It is a {@link SplitPane}
 * On left side: the list of connections
 * On right side: tabs, where each tab is connection details
 */
public class ConnectionsSplitPane extends SplitPane {

    private static final String READ_ME_TAB_ID = "readme";
    private final TreeView<String> connectionTreeView;
    private final TabPane rightSideTabPane;
    private final Map<String, Tab> connectionTabs = new ConcurrentHashMap<>();
    private final Map<String, ConnectionConfig> clusterConfigMap = new ConcurrentHashMap<>();

    public ConnectionsSplitPane() {
        setDividerPositions(0.2);
        rightSideTabPane = new TabPane();
        ConnectionConfigManager.readConnectionConfigs().forEach(connectionConfig -> {
            clusterConfigMap.put(connectionConfig.getConnectionId(), connectionConfig);
        });
        connectionTreeView = createConnectionsTreeView();
        getItems().addAll(connectionTreeView, rightSideTabPane);
        rightSideTabPane.prefWidthProperty().bind(widthProperty());
        rightSideTabPane.prefHeightProperty().bind(heightProperty());
    }

    private static String createConnectionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private TreeView<String> createConnectionsTreeView() {
        var connectionsTreeItems = new TreeItem<>("Connections");
        connectionsTreeItems.setExpanded(true);
        var treeView = new TreeView<String>();
        treeView.setOnMouseClicked(event -> {
            var selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.isLeaf()) {
                if (event.getClickCount() == 1) { // Single click

                } else if (event.getClickCount() == 2) { // Double click
                    var connectionId = selectedItem.getValue();
                    var tab = getOrCreateConnectionTab(connectionId);
                    rightSideTabPane.getSelectionModel().select(tab);
                }
            }
        });
        clusterConfigMap.keySet().forEach(connectionId -> {
            connectionsTreeItems.getChildren().add(new TreeItem<>(connectionId));
        });
        treeView.setRoot(connectionsTreeItems);
        return treeView;
    }

    public void createConnection() {
        var connectionId = createConnectionId();
        addConnectionToTreeView(connectionId);
        getOrCreateConnectionTab(connectionId);
    }

    private void addConnectionToTreeView(String connectionId) {
        var treeItem = new TreeItem<>(connectionId);
        connectionTreeView.getRoot().getChildren().add(treeItem);
    }

    private Tab getOrCreateConnectionTab(String connectionId) {
        var tabs = rightSideTabPane.getTabs();
        return tabs.stream()
                .filter(tab -> Objects.equals(tab.getId(), connectionId))
                .findFirst()
                .orElseGet(() -> {
                    var connectionTab = connectionTabs.computeIfAbsent(
                            connectionId,
                            id -> createConnectionTab(clusterConfigMap.getOrDefault(
                                    connectionId,
                                    ConnectionConfigManager.createDefaultClusterConfig(connectionId))));
                    tabs.add(connectionTab);
                    return connectionTab;
                });
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

    private Tab createConnectionTab(ConnectionConfig connectionConfig) {
        String connectionId = connectionConfig.getConnectionId();
        var connectionTab = new Tab(connectionId);
        connectionTab.setId(connectionId);
        var newConnectionPane = new ConnectionPane(connectionConfig);
        connectionTab.setContent(newConnectionPane);

        newConnectionPane.prefWidthProperty().bind(rightSideTabPane.widthProperty());
        newConnectionPane.prefHeightProperty().bind(rightSideTabPane.heightProperty());
        return connectionTab;
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
}
