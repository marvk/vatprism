package net.marvk.fs.vatsim.map.view.toolbar;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.Property;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import lombok.SneakyThrows;

public class ToolBarView implements FxmlView<ToolBarViewModel> {
    @FXML
    private ToggleButton enablePilotCallsign;
    @FXML
    private Button showInfo;
    @FXML
    private ToggleButton enableDebug;
    @FXML
    private ToggleButton autoReload;
    @InjectViewModel
    private ToolBarViewModel viewModel;

    @FXML
    private void refresh(final ActionEvent actionEvent) {
        viewModel.refresh();
    }

    public void setAutoReload(final ActionEvent actionEvent) {
        viewModel.setAutoReload(autoReload.isSelected());
    }

    public void initialize() {
        bindBoolean(enableDebug, viewModel.getPreferences().booleanProperty("metrics.show"));
        bindBoolean(enablePilotCallsign, viewModel.getPreferences().booleanProperty("pilots.label"));
    }

    private static void bindBoolean(final Toggle button, final Property<Boolean> other) {
        button.selectedProperty().bindBidirectional(other);
    }

    @SneakyThrows
    public void openSettings(final ActionEvent actionEvent) {
        viewModel.getPreferences().show();
    }

}
