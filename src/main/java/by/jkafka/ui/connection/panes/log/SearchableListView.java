package by.jkafka.ui.connection.panes.log;

import by.jkafka.ui.elements.CustomHBox;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

class SearchableListView extends VBox {
    protected static final String DELETE_LOGS_BUTTON_MSG = "Delete logs";
    protected static final String RESET_SEARCH_BUTTON_MSG = "X";
    private final ObservableList<String> elementList;

    SearchableListView(ObservableList<String> elementList) {
        this.elementList = elementList;
        setSpacing(5);
        setPadding(new Insets(5));
        getChildren().addAll(createNodes());
    }

    private List<Node> createNodes() {
        var filteredItems = new FilteredList<>(elementList, p -> true);
        var listView = new ListView<>(filteredItems);
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

        var resetSearchButton = new Button(RESET_SEARCH_BUTTON_MSG);
        resetSearchButton.setOnAction(e -> {
            searchField.clear();
        });

        var deleteLogsButton = new Button(DELETE_LOGS_BUTTON_MSG);
        deleteLogsButton.setOnAction(e -> {
            Logger.LOGGER.clear();
            elementList.setAll(List.of());
        });

        var searchHBox = new CustomHBox();
        searchHBox.getChildren().addAll(searchField, resetSearchButton, deleteLogsButton);

        return List.of(searchHBox, new Label("Logs:"), listView);
    }
}
