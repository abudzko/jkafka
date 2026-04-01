package by.jkafka.ui;

import by.jkafka.ui.connection.ConnectionsSplitPane;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RootPane extends BorderPane {
    private final Stage stage;
    private final JKafkaMenu jKafkaMenu;
    private final ConnectionsSplitPane connectionsSplitPane;

    public RootPane(Stage stage) {
        this.stage = stage;
        this.connectionsSplitPane = new ConnectionsSplitPane();
        this.jKafkaMenu = new JKafkaMenu(connectionsSplitPane);
        init();
    }

    void init() {
        setTop(jKafkaMenu);
        setCenter(connectionsSplitPane);
        var scene = new Scene(this, 900, 700);
        stage.setScene(scene);

        prefWidthProperty().bind(scene.widthProperty());
        prefHeightProperty().bind(scene.heightProperty());
    }
}
