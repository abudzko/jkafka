package by.jkafka.ui.elements;

import javafx.animation.Transition;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class CollapsiblePane extends VBox {
    private final Button headerButton;
    private final Pane content;
    private boolean expanded = true;

    public CollapsiblePane(String title, Pane content) {
        this.content = content;
        headerButton = new Button("▼ " + title);
        headerButton.setStyle(
                "-fx-background-color: #e0e0e0; " +
                        "-fx-padding: 8 12; " +
                        "-fx-cursor: hand; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 3;"
        );

        headerButton.setOnAction(e -> toggleCollapse());
        getChildren().addAll(headerButton, content);
        setSpacing(2);
    }

    private void toggleCollapse() {
        expanded = !expanded;

        // Animation ot change height
        Transition transition = new Transition() {
            {
                setCycleDuration(Duration.millis(150));
            }

            @Override
            protected void interpolate(double frac) {
                if (expanded) {
                    // Разворачиваем
                    content.setOpacity(frac);
                    content.setTranslateY((1 - frac) * -20);
                } else {
                    // Сворачиваем
                    content.setOpacity(1 - frac);
                    content.setTranslateY(frac * -20);
                }
            }
        };

        transition.setOnFinished(e -> {
            if (!expanded) {
                content.setVisible(false);
                content.setManaged(false);
            } else {
                content.setVisible(true);
                content.setManaged(true);
            }
            // Сбрасываем трансформации
            content.setOpacity(1.0);
            content.setTranslateY(0);
        });

        if (expanded) {
            // Подготавливаем к разворачиванию
            content.setVisible(true);
            content.setManaged(true);
            content.setOpacity(0);
            content.setTranslateY(-20);
            headerButton.setText("▼ " + headerButton.getText().substring(2));
        } else {
            headerButton.setText("▶ " + headerButton.getText().substring(2));
        }

        transition.play();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        if (this.expanded != expanded) {
            toggleCollapse();
        }
    }
}
