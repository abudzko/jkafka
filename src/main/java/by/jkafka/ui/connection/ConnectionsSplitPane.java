package by.jkafka.ui.connection;

import by.jkafka.config.ConnectionConfig;
import by.jkafka.config.ConnectionConfigManager;
import by.jkafka.ui.connection.panes.ConnectionPane;
import by.jkafka.ui.connection.panes.ReadmePane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;
import java.util.Objects;
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
                    var tab = connectionTabs.get(connectionId);
                    if (isConnectionTabOpen(connectionId)) {
                        rightSideTabPane.getSelectionModel().select(tab);
                    } else {
                        rightSideTabPane.getTabs().add(tab);
                        rightSideTabPane.getSelectionModel().select(tab);
                    }
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
        connectionTreeView.getRoot().getChildren().add(new TreeItem<>(connectionId));

        var connectionTab = new Tab(connectionId);
        connectionTab.setId(connectionId);
        var newConnectionPane = new ConnectionPane(connectionConfig);
        connectionTab.setContent(newConnectionPane);

        newConnectionPane.prefWidthProperty().bind(rightSideTabPane.widthProperty());
        newConnectionPane.prefHeightProperty().bind(rightSideTabPane.heightProperty());
        connectionTabs.put(connectionId, connectionTab);
        return connectionTab;
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
}
