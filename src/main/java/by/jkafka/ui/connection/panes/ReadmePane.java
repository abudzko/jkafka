package by.jkafka.ui.connection.panes;

import by.jkafka.utils.FileUtils;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ReadmePane extends Pane {
    public ReadmePane() {
        var vBox = new VBox();
        var textArea = new TextArea(FileUtils.readResource("/kafka/readme"));
        textArea.setEditable(false);
        vBox.getChildren().addAll(textArea);
        getChildren().addAll(vBox);
    }
}
