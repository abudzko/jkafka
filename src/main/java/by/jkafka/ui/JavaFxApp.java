package by.jkafka.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class JavaFxApp extends Application {
    public void launchApp(String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) {
        configureStage(stage);
        new RootPane(stage);
        stage.show();
    }

    private void configureStage(Stage stage) {
        stage.setTitle("JKafka");
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Platform.exit();
    }
}
