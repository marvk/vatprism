package net.marvk.fs.vatsim.map.view.menubar;

import de.saxsys.mvvmfx.FxmlView;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Window;

public class MenuBarView implements FxmlView<MenuBarViewModel> {
    @FXML
    private HBox container;
    private double x;
    private double y;

    public void initialize() {
        container.sceneProperty().addListener((observable, oldValue, newValue) -> addListenersToScene(newValue));
    }

    private void addListenersToScene(final Scene scene) {
        scene.windowProperty().addListener((observable1, oldValue1, newValue1) -> addListenersToWindow(newValue1));
    }

    private void addListenersToWindow(final Window window) {
        container.setOnMousePressed(mouseEvent -> {
            x = container.getScene().getWindow().getX() - mouseEvent.getScreenX();
            y = container.getScene().getWindow().getY() - mouseEvent.getScreenY();
        });

        container.setOnMouseDragged(mouseEvent -> {
            window.setX(mouseEvent.getScreenX() + x);
            window.setY(mouseEvent.getScreenY() + y);
        });
    }
}
