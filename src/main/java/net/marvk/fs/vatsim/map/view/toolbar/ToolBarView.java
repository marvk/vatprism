package net.marvk.fs.vatsim.map.view.toolbar;

import de.saxsys.mvvmfx.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.view.about.AboutView;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

public class ToolBarView implements FxmlView<ToolBarViewModel> {
    @FXML
    private ToggleButton fullScreen;
    @FXML
    private FontIcon fullScreenIcon;
    @FXML
    private HBox container;
    @FXML
    private ToggleButton enablePilotCallsign;
    @FXML
    private ToggleButton enableDebug;
    @FXML
    private ToggleButton autoReload;
    @InjectViewModel
    private ToolBarViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private void refresh(final ActionEvent actionEvent) {
        viewModel.refresh();
    }

    public void setAutoReload(final ActionEvent actionEvent) {
        viewModel.setAutoReload(autoReload.isSelected());
    }

    public void initialize() {
        container.sceneProperty().addListener((observable, oldValue, newValue) -> setupWindowBindings(newValue));

        bindBooleanBidirectional(enableDebug, "metrics.show");
        bindBooleanBidirectional(enablePilotCallsign, "pilots.show_label");

        enableDebug.visibleProperty().bind(booleanProperty("general.debug"));
    }

    private void setupWindowBindings(final Scene scene) {
        scene.windowProperty()
             .addListener((observable, oldValue, newValue) -> setupFullScreenBindings((Stage) newValue));
    }

    private void setupFullScreenBindings(final Stage stage) {
        final BooleanProperty fullScreenProperty = booleanProperty("general.fullscreen");
        stage.fullScreenProperty().addListener(
                (observable, oldValue, newValue) -> {
                    fullScreenProperty.set(newValue);
                }
        );

        fullScreenProperty.addListener((observable, oldValue, newValue) -> stage.setFullScreen(newValue));

        fullScreenIcon.iconCodeProperty().bind(Bindings.<Ikon>createObjectBinding(
                () -> ikon(fullScreenProperty.get()), fullScreenProperty
        ));

        bindBooleanBidirectional(fullScreen, fullScreenProperty);
    }

    private static Octicons ikon(final boolean fullscreen) {
        if (fullscreen) {
            return Octicons.SCREEN_NORMAL_16;
        }

        return Octicons.SCREEN_FULL_16;
    }

    private BooleanProperty booleanProperty(final String key) {
        return viewModel.getPreferences().booleanProperty(key);
    }

    private void bindBooleanBidirectional(final Toggle button, final String key) {
        bindBooleanBidirectional(button.selectedProperty(), key);
    }

    private void bindBooleanBidirectional(final Property<Boolean> property, final String key) {
        bindBooleanBidirectional(property, booleanProperty(key));
    }

    private static void bindBooleanBidirectional(final Toggle button, final Property<Boolean> property) {
        bindBooleanBidirectional(button.selectedProperty(), property);
    }

    private static void bindBooleanBidirectional(final Property<Boolean> property1, final Property<Boolean> property2) {
        property1.bindBidirectional(property2);
    }

    @SneakyThrows
    @FXML
    private void openSettings(final ActionEvent actionEvent) {
        viewModel.getPreferences().show();
    }

    @FXML
    private void showAbout(final ActionEvent actionEvent) {
        final Parent view = FluentViewLoader.fxmlView(AboutView.class).context(context).load().getView();

        final Popup popup = new Popup();
        popup.getContent().add(view);
        popup.setAutoHide(true);
        popup.show(container.getScene().getWindow());
    }
}
