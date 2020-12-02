package net.marvk.fs.vatsim.map.view.statusbar;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;

import java.text.DecimalFormat;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

public class StatusBarView implements FxmlView<StatusBarViewModel> {
    @FXML
    private Label playersOnline;
    @FXML
    private Label mousePosition;
    @FXML
    private Label highlightedFirs;
    @InjectViewModel
    private StatusBarViewModel viewModel;

    private final DecimalFormat df;

    {
        df = new DecimalFormat("#.######");
        df.setRoundingMode(HALF_UP);
    }

    public void initialize() {
        mousePosition.textProperty().bind(Bindings.createStringBinding(() -> {
            final Point2D p = viewModel.mouseWorldPositionProperty().get();

            final String x = df.format(p.getX()) + (p.getX() >= 0 ? "E" : "W");
            final String y = df.format(p.getY()) + (p.getY() >= 0 ? "N" : "S");

            return y + " " + x;
        }, viewModel.mouseWorldPositionProperty()));

        highlightedFirs.textProperty().bind(Bindings.createStringBinding(() ->
                        viewModel.getHighlightedFirs()
                                 .stream()
                                 .map(FlightInformationRegionBoundary::getIcao)
                                 .collect(Collectors.joining(", ")),
                viewModel.getHighlightedFirs()
        ));

        playersOnline.textProperty().bind(Bindings.createStringBinding(
                this::playerStatsString, viewModel.playerStatsProperty()
        ));
    }

    private String playerStatsString() {
        final PlayerStats playerStats = viewModel.getPlayerStats();
        final StringJoiner sj = new StringJoiner(", ");

        sj.add(playerStats.getPilots() + " Pilots");
        sj.add(playerStats.getControllers() + " Controllers");
        sj.add(playerStats.getObservers() + " Observers");

        return sj.toString();
    }
}
