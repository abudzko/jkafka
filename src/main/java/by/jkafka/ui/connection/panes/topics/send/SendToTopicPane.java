package by.jkafka.ui.connection.panes.topics.send;

import by.jkafka.kafka.KafkaConnection;
import by.jkafka.ui.connection.panes.topics.TopicProvider;
import by.jkafka.ui.elements.CustomHBox;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendToTopicPane extends Pane {

    private static final String SEND_TO_TOPIC_BUTTON_MSG = "Send";
    private final Button sendButton;
    private final KafkaConnection kafkaConnection;
    private final TopicProvider topicProvider;
    private TextArea keyTextArea;
    private TextArea headersTextArea;
    private TextArea messageTextArea;

    public SendToTopicPane(
            KafkaConnection kafkaConnection,
            TopicProvider topicProvider
    ) {
        this.kafkaConnection = kafkaConnection;
        this.topicProvider = topicProvider;
        var hBox = new CustomHBox();
        sendButton = createSendToTopicButton();
        hBox.getChildren().addAll(sendButton);

        var inputsTextAreaPane = createInputsTextAreaPane();
        var vBox = new VBox();
        vBox.getChildren().addAll(hBox, inputsTextAreaPane);
        getChildren().addAll(vBox);
    }

    private SplitPane createInputsTextAreaPane() {
        keyTextArea = new TextArea();
        keyTextArea.setPromptText("Message key");
        headersTextArea = new TextArea();
        headersTextArea.setPromptText("Message headers: headerName=headerValue");
        messageTextArea = new TextArea();
        messageTextArea.setPromptText("Message text");
        var inputsTextAreaPane = new SplitPane();
        inputsTextAreaPane.setDividerPositions(0.1, 0.1, 0.8);
        inputsTextAreaPane.setOrientation(Orientation.VERTICAL);
        inputsTextAreaPane.getItems().addAll(keyTextArea, headersTextArea, messageTextArea);
        inputsTextAreaPane.prefWidthProperty().bind(widthProperty());
        return inputsTextAreaPane;
    }

    private Button createSendToTopicButton() {
        final Button sendButton;
        sendButton = new Button(SEND_TO_TOPIC_BUTTON_MSG);
        sendButton.setOnMouseClicked(event -> {
            var topic = topicProvider.topic();
            if (topic != null) {
                var sendingMessage = SendingMessage.builder()
                        .topic(topic)
                        .headers(parseHeaders())
                        .key(keyTextArea.getText())
                        .messages(List.of(messageTextArea.getText()))
                        .build();
                kafkaConnection.send(sendingMessage);
            }
        });
        return sendButton;
    }

    private Map<String, String> parseHeaders() {
        var headers = new HashMap<String, String>();
        headersTextArea.getText();
        return headers;
    }
}
