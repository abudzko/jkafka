package by.jkafka.ui.connection.panes.log;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogPane extends Pane {
    private final ObservableList<String> elementList;

    public LogPane() {
        elementList = FXCollections.observableArrayList();
        var searchableListView = new SearchableListView(elementList);

        getChildren().add(searchableListView);
        searchableListView.prefWidthProperty().bind(widthProperty());
        var t = new Thread(() -> {
            while (true) {
                try {
                    updateView();
                } catch (Throwable e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @SneakyThrows
    private void updateView() {
        AtomicBoolean newLogs = Logger.LOGGER.getNewLogs();
        if (newLogs.get()) {
            newLogs.set(false);
            var countDownLatch = new CountDownLatch(1);
            Platform.runLater(() -> {
                elementList.setAll(new ArrayList<>(Logger.LOGGER.getLogs()));
                countDownLatch.countDown();
            });
            countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        } else {
            Thread.sleep(500);
        }
    }
}
