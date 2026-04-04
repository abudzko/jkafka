package by.jkafka.ui.connection.panes.topics;

import by.jkafka.kafka.KafkaConnection;
import by.jkafka.kafka.read.OnReadTopicCallbacks;
import by.jkafka.ui.connection.panes.topics.read.ReadFromTopicPane;
import by.jkafka.ui.connection.panes.topics.send.SendToTopicPane;
import by.jkafka.ui.elements.CustomHBox;
import by.jkafka.utils.logs.LogEvent;
import by.jkafka.utils.logs.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaTopicsPane extends Pane {

    private static final String REFRESH_TOPIC_LIST_BUTTON_MSG = "Refresh topics";
    private static final String RESET_SEARCH_BUTTON_MSG = "X";
    private final KafkaConnection kafkaConnection;
    private final ObservableList<String> topicList = FXCollections.observableArrayList();
    /**
     * List of topics(left), READ/WRITE(right)
     */
    private final SplitPane splitPane;
    /**
     * Ech topic has it own tab
     */
    private final Map<String, TabPane> selectedTopicTabPanes = new ConcurrentHashMap<>();
    private final VBox selectedTopicTabPaneBox;
    private final Set<String> readingStartedTopics = ConcurrentHashMap.newKeySet();
    private volatile String selectedTopic;
    private ListView<String> topicsListView;

    public KafkaTopicsPane(KafkaConnection kafkaConnection) {
        this.kafkaConnection = kafkaConnection;
        splitPane = new SplitPane();
        splitPane.prefWidthProperty().bind(widthProperty());
        splitPane.prefHeightProperty().bind(heightProperty());

        var topicsVBox = createTopicsVBox();
        selectedTopicTabPaneBox = new VBox();
        splitPane.getItems().addAll(topicsVBox, selectedTopicTabPaneBox);
        getChildren().addAll(splitPane);
    }

    private static TextField createTopicSearchByTextField(FilteredList<String> filteredItems) {
        var searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredItems.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return item.toLowerCase().contains(newValue.toLowerCase());
            });
        });
        return searchField;
    }

    private Button createResetTopicSearchButton(ListView<String> topicsListView, TextField searchField) {
        var resetSearchButton = new Button(RESET_SEARCH_BUTTON_MSG);
        resetSearchButton.setOnAction(e -> {
            resetSelectedTopic();
            topicsListView.getSelectionModel().clearSelection();
            searchField.clear();
        });
        return resetSearchButton;
    }

    /**
     * Refresh button, search in topics list
     */
    private VBox createTopicsVBox() {
        var topicsVBox = new VBox();
        var buttonHBox = new CustomHBox();
        buttonHBox.getChildren().addAll(createRefreshTopicsButton());
        topicsVBox.getChildren().addAll(buttonHBox, topicsVBox());
        return topicsVBox;
    }

    private TabPane createSelectedTopicTabPane(SplitPane root) {
        var selectedTopicTabPane = new TabPane();
        selectedTopicTabPane.prefWidthProperty().bind(root.widthProperty());
        selectedTopicTabPane.getTabs().addAll(
                createReadFromTopicTab(selectedTopicTabPane),
                createSendToTopicTab(selectedTopicTabPane)
        );
        return selectedTopicTabPane;
    }

    private Tab createReadFromTopicTab(TabPane root) {
        var id = "READ";
        var readFromTopicTab = new Tab(id);
        readFromTopicTab.setClosable(false);
        readFromTopicTab.setId(id);
        var onReadTopicCallbacks = new OnReadTopicCallbacks() {
            @Override
            public void startReading(String topic) {
                readingStartedTopics.add(topic);
                Platform.runLater(() -> {
                    topicsListView.refresh();
                });
            }

            @Override
            public void stopReading(String topic) {
                readingStartedTopics.remove(topic);
                Platform.runLater(() -> {
                    topicsListView.refresh();
                });

            }
        };
        var readFromTopicPane = new ReadFromTopicPane(
                kafkaConnection,
                () -> selectedTopic,
                onReadTopicCallbacks
        );
        readFromTopicTab.setContent(readFromTopicPane);

        readFromTopicPane.prefWidthProperty().bind(root.widthProperty());
        return readFromTopicTab;
    }

    private Tab createSendToTopicTab(TabPane root) {
        String id = "SEND";
        var sendToTopicTab = new Tab(id);
        sendToTopicTab.setClosable(false);
        sendToTopicTab.setId(id);
        var sendToTopicPane = new SendToTopicPane(kafkaConnection, () -> selectedTopic);
        sendToTopicTab.setContent(sendToTopicPane);

        sendToTopicPane.prefWidthProperty().bind(root.widthProperty());
        return sendToTopicTab;
    }

    private VBox topicsVBox() {
        var filteredItems = new FilteredList<>(topicList, p -> true);
        var searchField = createTopicSearchByTextField(filteredItems);

        var topicsListView = new ListView<>(filteredItems);
        topicsListView.setPrefHeight(200);
        topicsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        var onTopicSelectedListener = createOnTopicSelectedListener();

        topicsListView.getSelectionModel().selectedItemProperty().addListener(onTopicSelectedListener);
        topicsListView.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    boolean isSelected = item.equals(selectedTopic);
                    if (readingStartedTopics.contains(item)) {
                        if (isSelected) {
                            setStyle("-fx-background-color: #40f32a; -fx-text-fill: black; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-background-color: #40f32a; -fx-text-fill: black;");
                        }
                    } else {
                        if (isSelected) {
                            setStyle("-fx-background-color: #c5d0f2; -fx-text-fill: black;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            }
        });
        this.topicsListView = topicsListView;

        var resetSearchButton = createResetTopicSearchButton(topicsListView, searchField);

        var searchHBox = new CustomHBox();
        searchHBox.getChildren().addAll(searchField, resetSearchButton);

        var root = new VBox(5);
        root.setPadding(new Insets(5));
        root.getChildren().addAll(
                searchHBox,
                new Label("Topics:"),
                topicsListView
        );
        return root;
    }

    private ChangeListener<String> createOnTopicSelectedListener() {
        return (observable, oldValue, newValue) -> {
            selectedTopicTabPaneBox.getChildren().clear();
            if (newValue != null) {
                selectedTopic = newValue;
                var selectedTopicTabPane = selectedTopicTabPanes.computeIfAbsent(
                        selectedTopic,
                        topic -> createSelectedTopicTabPane(splitPane)
                );
                selectedTopicTabPaneBox.getChildren().addAll(selectedTopicTabPane);
                var log = "Selected topic: " + newValue;
                log(log);
            } else {
                String log = "Selected topic: nothing selected. Previous value " + oldValue;
                log(log);
            }
        };
    }

    /**
     * Getting the list of topics from kafka cluster and refreshes them in UI
     */
    private Button createRefreshTopicsButton() {
        var refresh = new Button(REFRESH_TOPIC_LIST_BUTTON_MSG);
        refresh.setOnMouseClicked(new EventHandler<>() {
            @Override
            public synchronized void handle(MouseEvent event) {
                var thread = new Thread(() -> {
                    try {
                        log("Topics refreshing ...");
                        resetSelectedTopic();
                        var topics = kafkaConnection.getTopics();
                        Platform.runLater(() -> {
                            topicList.setAll(topics);
                        });
                    } catch (Exception e) {
                        log(e.getMessage());
                        Logger.LOGGER.debug(e);
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }
        });
        return refresh;
    }

    private void resetSelectedTopic() {
        selectedTopic = null;
    }

    private void log(String log) {
        var connectionId = kafkaConnection.getConnectionConfig().getConnectionId();
        Logger.LOGGER.log(LogEvent.builder().connectionId(connectionId).log(log).build());
    }
}
