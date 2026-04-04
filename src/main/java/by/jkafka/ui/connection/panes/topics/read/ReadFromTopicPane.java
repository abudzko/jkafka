package by.jkafka.ui.connection.panes.topics.read;

import by.jkafka.kafka.KafkaConnection;
import by.jkafka.kafka.read.OnReadTopicCallbacks;
import by.jkafka.kafka.read.TopicReader;
import by.jkafka.kafka.read.filters.NoopEventFilter;
import by.jkafka.kafka.read.filters.StringValueEventFilter;
import by.jkafka.ui.connection.panes.topics.TopicProvider;
import by.jkafka.ui.elements.CustomHBox;
import by.jkafka.utils.StringUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ReadFromTopicPane extends Pane {

    private static final String READ_TOPIC_BUTTON_MSG = "Read";
    private static final String STOP_READING_TOPIC_BUTTON_MSG = "Stop";
    private final KafkaConnection kafkaConnection;
    private final TopicProvider topicProvider;
    private final OnReadTopicCallbacks onReadTopicCallbacks;
    private final ConcurrentHashMap<String, TopicReader> readers = new ConcurrentHashMap<>();
    private final ConsumerRecordService consumerRecordService;
    private final Button readButton;
    private final Button refreshInfoButton;
    private final TextArea infoTextArea;
    private final String connectionId;


    public ReadFromTopicPane(
            KafkaConnection kafkaConnection,
            TopicProvider topicProvider,
            OnReadTopicCallbacks onReadTopicCallbacks
    ) {
        this.kafkaConnection = kafkaConnection;
        this.connectionId = kafkaConnection.getConnectionConfig().getConnectionId();
        this.topicProvider = topicProvider;
        this.onReadTopicCallbacks = onReadTopicCallbacks;
        this.consumerRecordService = new ConsumerRecordService();
        var hBox = new CustomHBox();
        readButton = createReadTopicButton();

        refreshInfoButton = createRefreshInfoButton();
        hBox.getChildren().addAll(readButton, refreshInfoButton);

        infoTextArea = new TextArea();
        infoTextArea.setEditable(false);
        var vBox = new VBox();
        vBox.getChildren().addAll(hBox, createEventFilter(), infoTextArea);

        getChildren().addAll(vBox);
        refreshInfo();
    }

    private HBox createEventFilter() {
        var hbox = new HBox();
        var eventFilterInputField = new TextField();
        eventFilterInputField.setPromptText("Filter...");
        var applyFilterButton = new Button("Apply");
        applyFilterButton.setOnMouseClicked(event -> {
            Optional.ofNullable(topicProvider.topic()).flatMap(topic -> Optional.ofNullable(readers.get(topic)))
                    .ifPresent(reader -> {
                        var filterValue = eventFilterInputField.getText();
                        if (StringUtils.hasLength(filterValue)) {
                            reader.updateFilter(new StringValueEventFilter(filterValue));
                        } else {
                            reader.updateFilter(new NoopEventFilter());
                        }
                    });
        });

        var resetFilterButton = new Button("X");
        resetFilterButton.setOnAction(e -> {
            Optional.ofNullable(topicProvider.topic()).flatMap(topic -> Optional.ofNullable(readers.get(topic)))
                    .ifPresent(reader -> {
                        eventFilterInputField.clear();
                        reader.updateFilter(new NoopEventFilter());
                    });
        });
        hbox.getChildren().addAll(eventFilterInputField, resetFilterButton, applyFilterButton);
        return hbox;
    }

    private Button createRefreshInfoButton() {
        var refreshButton = new Button("Refresh");
        refreshButton.setOnMouseClicked(e -> {
            refreshInfo();
        });
        return refreshButton;
    }

    private void refreshInfo() {
        var topic = topicProvider.topic();
        var topicInfoOptional = kafkaConnection.getTopicInfo(topic);
        topicInfoOptional.ifPresent(topicInfo -> {
            var now = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now());
            var builder = new StringBuilder();
            builder.append(now).append(System.lineSeparator())
                    .append("Topic: ").append(topicInfo.getTopic()).append(System.lineSeparator());
            topicInfo.getPartitions().forEach(p -> {
                builder.append("Partition: ").append(p.getPartition())
                        .append(". Commited offset = ").append(p.getCommitedOffset())
                        .append(System.lineSeparator());
            });

            Optional.ofNullable(readers.get(topic)).ifPresent(reader -> {
                builder.append("Read events: ").append(reader.readEventsCount());
            });

            infoTextArea.setText(builder.toString());
        });

    }

    private Button createReadTopicButton() {
        var readButton = new Button(READ_TOPIC_BUTTON_MSG);
        readButton.setOnMouseClicked(event -> {
            var topic = topicProvider.topic();
            if (topic != null) {
                var reader = readers.computeIfAbsent(
                        topic,
                        key -> kafkaConnection.topicReader(topic, new OnReadTopicCallbacks() {
                            @Override
                            public void startReading(String topic) {
                                onReadTopicCallbacks.startReading(topic);
                            }

                            @Override
                            public void stopReading(String topic) {
                                Platform.runLater(() -> {
                                    readButton.setText(READ_TOPIC_BUTTON_MSG);
                                });
                                onReadTopicCallbacks.stopReading(topic);
                            }

                            @Override
                            public void consume(ConsumerRecord<String, Object> record) {
                                onReadTopicCallbacks.consume(record);
                                consumerRecordService.saveRecord(connectionId, record);
                            }
                        })
                );
                if (reader.isStarted()) {
                    onReadTopicCallbacks.stopReading(topic);
                    reader.stop();
                    readButton.setText(READ_TOPIC_BUTTON_MSG);
                } else {
                    reader.start();
                    onReadTopicCallbacks.startReading(topic);
                    readButton.setText(STOP_READING_TOPIC_BUTTON_MSG);
                }
            }
        });
        return readButton;
    }
}
