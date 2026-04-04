package by.jkafka.ui.connection.panes;

import by.jkafka.config.ConnectionConfig;
import by.jkafka.kafka.KafkaConnection;
import by.jkafka.ui.connection.panes.log.LogPane;
import by.jkafka.ui.connection.panes.topics.KafkaTopicsPane;
import by.jkafka.ui.elements.CollapsiblePane;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ConnectionPane extends Pane {

    private final KafkaConnection kafkaConnection;
    private ClusterConfigPane clusterConfigPane;
    private KafkaTopicsPane kafkaTopicsPane;
    private LogPane logsPane;

    public ConnectionPane(ConnectionConfig connectionConfig) {
        kafkaConnection = new KafkaConnection(connectionConfig);
        initPane();
    }

    private void initPane() {
        var topVBox = new VBox();
        clusterConfigPane = new ClusterConfigPane(kafkaConnection);
        kafkaTopicsPane = new KafkaTopicsPane(kafkaConnection);

        var clusterConfigPane = new CollapsiblePane("Cluster config", this.clusterConfigPane);
        clusterConfigPane.setExpanded(false);
        var topicsPane = new CollapsiblePane("Topics", this.kafkaTopicsPane);
        clusterConfigPane.prefWidthProperty().bind(widthProperty());

        topicsPane.prefWidthProperty().bind(widthProperty());
        topicsPane.prefHeightProperty().bind(heightProperty());
        kafkaTopicsPane.prefHeightProperty().bind(topicsPane.heightProperty());

        topVBox.getChildren().addAll(clusterConfigPane, topicsPane);
        var topScrollPane = new ScrollPane();

        topScrollPane.setPannable(true);
        topScrollPane.setContent(topVBox);
        topScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        topScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        var logsScrollPane = createLogsScrollPane();
        var splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.7);
        splitPane.getItems().addAll(topScrollPane, logsScrollPane);
        splitPane.prefWidthProperty().bind(widthProperty());
        splitPane.prefHeightProperty().bind(heightProperty());
        getChildren().add(splitPane);
    }

    private ScrollPane createLogsScrollPane() {
        this.logsPane = new LogPane(kafkaConnection.getConnectionConfig().getConnectionId());
        logsPane.prefWidthProperty().bind(widthProperty());

        var scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        var collapsiblePane = new CollapsiblePane("Logs", this.logsPane);
        scrollPane.setContent(collapsiblePane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }
}
