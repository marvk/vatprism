package net.marvk.fs.vatsim.map.view.toolbar;

import de.saxsys.mvvmfx.*;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.view.about.AboutView;

public class ToolBarView implements FxmlView<ToolBarViewModel> {
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
        bindBoolean(enableDebug, viewModel.getPreferences().booleanProperty("metrics.show"));
        bindBoolean(enablePilotCallsign, viewModel.getPreferences().booleanProperty("pilots.show_label"));

        enableDebug.visibleProperty().bind(viewModel.getPreferences().booleanProperty("general.debug"));
    }

    private static void bindBoolean(final Toggle button, final Property<Boolean> other) {
        button.selectedProperty().bindBidirectional(other);
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
