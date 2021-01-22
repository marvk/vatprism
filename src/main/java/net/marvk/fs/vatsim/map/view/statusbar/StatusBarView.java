package net.marvk.fs.vatsim.map.view.statusbar;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import net.marvk.fs.vatsim.map.view.BindingsUtil;

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
            final StringJoiner sj = new StringJoiner(", ");

            sj.add(playerStats.getPilots() + " Pilots");
            sj.add(playerStats.getControllers() + " Controllers");
            sj.add(playerStats.getObservers() + " Observers");

            playersOnline.setText(sj.toString());
        });
    }

    private static void bindTextToTooltip(final Label label) {
        final Tooltip value = new Tooltip();
        value.textProperty().bind(label.textProperty());
        label.setTooltip(value);
    }
}
