package by.jkafka.ui;

import by.jkafka.ui.connection.ConnectionsSplitPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

public class JKafkaMenu extends MenuBar {

    private final ConnectionsSplitPane connectionsSplitPane;

    public JKafkaMenu(ConnectionsSplitPane connectionsSplitPane) {
        this.connectionsSplitPane = connectionsSplitPane;
        init();
    }

    public void init() {
        var menu = new Menu("Menu");
        var newConnectionMenuItem = createConnectionMenuItem();
        var infoMenuItem = readmeMenuItem();
        menu.getItems().addAll(newConnectionMenuItem, infoMenuItem);
        getMenus().add(menu);
    }

    private MenuItem readmeMenuItem() {
        var infoMenuItem = new MenuItem("Readme");
        infoMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                connectionsSplitPane.getOrCreateReadmeTab();
            }
        });
        return infoMenuItem;
    }

    private MenuItem createConnectionMenuItem() {
        var newConnectionMenuItem = new MenuItem("New connection");
        newConnectionMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                connectionsSplitPane.createConnection();
            }
        });
        return newConnectionMenuItem;
    }
}
