package by.jkafka.ui.connection.panes.config;

import by.jkafka.config.ConfigConstants;
import by.jkafka.config.ConnectionConfigManager;
import by.jkafka.kafka.KafkaConnection;
import by.jkafka.ui.elements.CustomHBox;
import by.jkafka.utils.logs.LogEvent;
import by.jkafka.utils.logs.Logger;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public class UiConfigPane extends Pane {

    private static final String APPLY_CONFIGURATION_BUTTON = "Apply";
    private static final String UI_CONFIGURATION_SAVED_MSG = "UI configuration: saved";

    private final KafkaConnection kafkaConnection;
    private final Map<String, TextField> fieldMap = new LinkedHashMap<>();

    public UiConfigPane(KafkaConnection kafkaConnection) {
        this.kafkaConnection = kafkaConnection;
        var propsBox = createPropsBox();
        var vBox = new VBox();
        vBox.getChildren().addAll(propsBox, createButtonHBox());
        getChildren().addAll(vBox);
    }

    private CustomHBox createButtonHBox() {
        var buttonHBox = new CustomHBox();
        var saveConfigButton = createSaveConfigButton();
        buttonHBox.getChildren().addAll(saveConfigButton);
        return buttonHBox;
    }

    private VBox createPropsBox() {
        var vBox = new VBox();
        var uiConfig = kafkaConnection.getConnectionConfig().getUiConfig();
        ConfigConstants.CONNECTION_PROPS.forEach(field -> {
            var hBox = new CustomHBox();
            var textField = new TextField();
            textField.setText(uiConfig.get(field));
            fieldMap.put(field, textField);
            hBox.getChildren().addAll(new Label(field), textField);
            vBox.getChildren().add(hBox);
        });
        return vBox;
    }


    private void refreshUiConfig() {
        var configMap = new LinkedHashMap<String, String>();
        fieldMap.forEach((name, textField) -> configMap.put(name, textField.getText()));
        kafkaConnection.getConnectionConfig().updateUiConfig(configMap);
    }

    private Button createSaveConfigButton() {
        var save = new Button(APPLY_CONFIGURATION_BUTTON);
        save.setOnMouseClicked(new EventHandler<>() {
            @Override
            public synchronized void handle(MouseEvent event) {
                var thread = new Thread(() -> {
                    try {
                        refreshUiConfig();
                        ConnectionConfigManager.saveConfig(kafkaConnection.getConnectionConfig());
                        log(UI_CONFIGURATION_SAVED_MSG);
                    } catch (Exception e) {
                        log(e.getMessage());
                        Logger.LOGGER.debug(e);
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });
        return save;
    }

    private void log(String log) {
        var connectionId = kafkaConnection.getConnectionConfig().getConnectionId();
        Logger.LOGGER.log(LogEvent.builder().connectionId(connectionId).log(log).build());
    }
}
