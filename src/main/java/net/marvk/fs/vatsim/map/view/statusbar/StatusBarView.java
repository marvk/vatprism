package net.marvk.fs.vatsim.map.view.statusbar;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectResourceBundle;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import net.marvk.fs.vatsim.map.view.BindingsUtil;

import java.util.ResourceBundle;
import java.util.StringJoiner;

public class StatusBarView implements FxmlView<StatusBarViewModel> {
    @FXML
    private Label playersOnline;
    @FXML
    private Label mousePosition;
    @FXML
    private Label information;
    @InjectViewModel
    private StatusBarViewModel viewModel;
    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    public void initialize() {
        mousePosition.textProperty().bind(BindingsUtil.position(viewModel.mouseWorldPositionProperty()));

        information.textProperty().bind(viewModel.informationProperty());

        viewModel.playerStatsProperty().addListener((observable, oldValue, newValue) -> setPlayerStats());
        setPlayerStats();

        bindTextToTooltip(playersOnline);
        bindTextToTooltip(information);
        bindTextToTooltip(mousePosition);
    }

    private void setPlayerStats() {
        final PlayerStats playerStats = viewModel.getPlayerStats();
        Platform.runLater(() -> {
            final StringJoiner sj = new StringJoiner(resourceBundle.getString("status_bar.delimiter"));

            sj.add(resolveCountString(playerStats.getPilots(), "status_bar.pilots_count"));
            sj.add(resolveCountString(playerStats.getControllers(), "status_bar.controller_count"));
            sj.add(resolveCountString(playerStats.getObservers(), "status_bar.observers_count"));

            playersOnline.setText(sj.toString());
        });
    }

    private String resolveCountString(final int count, final String key) {
        return resourceBundle.getString(key).replace("{count}", Integer.toString(count));
    }

    private static void bindTextToTooltip(final Label label) {
        final Tooltip value = new Tooltip();
        value.textProperty().bind(label.textProperty());
        label.setTooltip(value);
    }
}
