package net.marvk.fs.vatsim.map.view.statusbar;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.BindingsUtil;

import java.util.StringJoiner;
import java.util.stream.Collectors;

public class StatusBarView implements FxmlView<StatusBarViewModel> {
    @FXML
    private Label playersOnline;
    @FXML
    private Label mousePosition;
    @FXML
    private Label highlightedFirs;
    @InjectViewModel
    private StatusBarViewModel viewModel;

    public void initialize() {
        mousePosition.textProperty().bind(BindingsUtil.position(viewModel.mouseWorldPositionProperty()));

        highlightedFirs.textProperty().bind(Bindings.createStringBinding(() ->
                        viewModel.getHighlightedFirs()
                                 .stream()
                                 .map(FlightInformationRegionBoundary::getIcao)
                                 .collect(Collectors.joining(", ")),
                viewModel.getHighlightedFirs()
        ));

        viewModel.playerStatsProperty().addListener((observable, oldValue, newValue) -> setPlayerStats());
        setPlayerStats();

        bindTextToTooltip(playersOnline);
        bindTextToTooltip(highlightedFirs);
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
