package by.jkafka.ui.connection.panes.config;

import by.jkafka.kafka.KafkaConnection;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

public class ConfigPane extends Pane {

    private final KafkaConnection kafkaConnection;

    public ConfigPane(KafkaConnection kafkaConnection) {
        this.kafkaConnection = kafkaConnection;
        var configTabPane = new TabPane();
        configTabPane.prefWidthProperty().bind(widthProperty());

        var clusterConfigTab = createClusterConfigTab(configTabPane);
        var uiConfigTab = createUirConfigTab(configTabPane);
        configTabPane.getTabs().addAll(clusterConfigTab, uiConfigTab);

        getChildren().addAll(configTabPane);
    }

    private Tab createClusterConfigTab(TabPane root) {
        var id = "Cluster configs";
        var clusterConfigTab = new Tab(id);
        clusterConfigTab.setClosable(false);
        clusterConfigTab.setId(id);
        var clusterConfigPane = new ClusterConfigPane(kafkaConnection);
        clusterConfigPane.prefWidthProperty().bind(root.widthProperty());
        clusterConfigTab.setContent(clusterConfigPane);
        return clusterConfigTab;
    }

    private Tab createUirConfigTab(TabPane root) {
        var id = "Other configs";
        var uiConfigTab = new Tab(id);
        uiConfigTab.setClosable(false);
        uiConfigTab.setId(id);
        var uiConfigPane = new UiConfigPane(kafkaConnection);
        uiConfigPane.prefWidthProperty().bind(root.widthProperty());
        uiConfigTab.setContent(uiConfigPane);
        return uiConfigTab;
    }
}
