package by.jkafka.ui.connection.panes.log;

import by.jkafka.utils.logs.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LogPane extends Pane {
    private final ObservableList<String> elementList;
    private final String connectionId;

    public LogPane(String connectionId) {
        this.connectionId = connectionId;
        elementList = FXCollections.observableArrayList();
        var searchableListView = new SearchableListView(connectionId, elementList);

        getChildren().add(searchableListView);
        searchableListView.prefWidthProperty().bind(widthProperty());
        var t = new Thread(() -> {
            while (true) {
                try {
                    updateView();
                } catch (Throwable e) {
                    Logger.LOGGER.debug(e);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @SneakyThrows
    private void updateView() {
        var newLogs = Logger.LOGGER.getLogs(connectionId);
        if (!newLogs.isEmpty()) {
            var countDownLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                elementList.setAll(newLogs);
                countDownLatch.countDown();
            });
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } else {
            Thread.sleep(500);
        }
    }
}
