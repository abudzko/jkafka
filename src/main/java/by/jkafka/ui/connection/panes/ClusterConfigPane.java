package by.jkafka.ui.connection.panes;

import by.jkafka.config.ConnectionConfigManager;
import by.jkafka.config.ConfigUtils;
import by.jkafka.kafka.KafkaConnection;
import by.jkafka.ui.connection.panes.log.Logger;
import by.jkafka.ui.elements.CustomHBox;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.SneakyThrows;

public class ClusterConfigPane extends Pane {

    private static final String SAVE_CONFIGURATION_BUTTON = "Save configuration";
    private static final String TEST_CONNECTION_BUTTON = "Test connection";
    private static final String CONNECTION_CONFIGURATION_SAVED_MSG = "Connection configuration: saved";

    private final KafkaConnection kafkaConnection;
    private final TextArea configTextArea;

    public ClusterConfigPane(KafkaConnection kafkaConnection) {
        this.kafkaConnection = kafkaConnection;
        configTextArea = createConfigTextArea();
        configTextArea.prefWidthProperty().bind(widthProperty());

        refreshClusterConfig();

        var vBox = new VBox();
        vBox.getChildren().addAll(configTextArea, createButtonHBox());
        getChildren().addAll(vBox);
    }

    @SneakyThrows
    private TextArea createConfigTextArea() {
        var clusterConfig = kafkaConnection.getConnectionConfig();
        return new TextArea(ConfigUtils.toStr(clusterConfig.getKafkaConfig()));
    }

    private CustomHBox createButtonHBox() {
        var buttonHBox = new CustomHBox();
        var testConnectionButton = createTestConnectionButton();
        var saveConfigButton = createSaveConfigButton();
        buttonHBox.getChildren().addAll(testConnectionButton, saveConfigButton);
        return buttonHBox;
    }

    private void refreshClusterConfig() {
        kafkaConnection.getConnectionConfig().setKafkaConfig(ConfigUtils.parseConfig(configTextArea.getText()));
    }

    private Button createTestConnectionButton() {
        var test = new Button(TEST_CONNECTION_BUTTON);
        test.setOnMouseClicked(new EventHandler<>() {
            @Override
            public synchronized void handle(MouseEvent event) {
                var thread = new Thread(() -> {
                    try {
                        kafkaConnection.testConnection();
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        log(e.getMessage());
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });
        return test;
    }

    private Button createSaveConfigButton() {
        var save = new Button(SAVE_CONFIGURATION_BUTTON);
        save.setOnMouseClicked(new EventHandler<>() {
            @Override
            public synchronized void handle(MouseEvent event) {
                var thread = new Thread(() -> {
                    try {
                        refreshClusterConfig();
                        ConnectionConfigManager.saveConfig(kafkaConnection.getConnectionConfig());
                        System.out.println(CONNECTION_CONFIGURATION_SAVED_MSG);
                        log(CONNECTION_CONFIGURATION_SAVED_MSG);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                        log(e.getMessage());
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });
        return save;
    }

    private void log(String log) {
        Logger.LOGGER.log(log);
    }
}
