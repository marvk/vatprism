package net.marvk.fs.vatsim.map.view.datadetail;

import de.saxsys.mvvmfx.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.DataVisitor;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.airportdetail.AirportDetailView;
import net.marvk.fs.vatsim.map.view.pilotdetail.PilotDetailView;

import java.util.Optional;

public class DataDetailView implements FxmlView<DataDetailViewModel> {
    @FXML
    private Button historyForward;

    @FXML
    private Button historyBack;

    @FXML
    private VBox container;

    @InjectViewModel
    private DataDetailViewModel viewModel;

    @InjectContext
    private Context context;

    private PaneManager paneManager;

    public void initialize() {
        paneManager = new PaneManager(context);

        viewModel.dataProperty().addListener((observable, oldValue, newValue) -> {
            setPane(paneManager.visitNullSafe(newValue));
        });

        historyBack.disableProperty().bind(viewModel.historyBackAvailableProperty().not());
        historyForward.disableProperty().bind(viewModel.historyForwardAvailableProperty().not());
    }

    private void setPane(final Optional<Parent> visit) {
        container.getChildren().clear();
        visit.ifPresent(e -> container.getChildren().add(e));
    }

    public void hide(final ActionEvent actionEvent) {
        viewModel.dataProperty().set(null);
    }

    public void historyBack(final ActionEvent actionEvent) {
        viewModel.historyBack();
    }

    public void historyForward(final ActionEvent actionEvent) {
        viewModel.historyForward();
    }

    private static class PaneManager implements DataVisitor<Parent> {
        private final ViewTuple<PilotDetailView, DataDetailSubViewModel<Pilot>> pilotDetailView;

        private final ViewTuple<AirportDetailView, DataDetailSubViewModel<Airport>> airportDetailView;

        public PaneManager(final Context context) {
            this.pilotDetailView = FluentViewLoader
                    .fxmlView(PilotDetailView.class)
                    .context(context)
                    .load();

            this.airportDetailView = FluentViewLoader
                    .fxmlView(AirportDetailView.class)
                    .context(context)
                    .load();
        }

        @Override
        public Parent visit(final Airport airport) {
            airportDetailView.getViewModel().setData(airport);
            return airportDetailView.getView();
        }

        @Override
        public Parent visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            return null;
        }

        @Override
        public Parent visit(final Pilot pilot) {
            pilotDetailView.getViewModel().setData(pilot);
            return pilotDetailView.getView();
        }
    }
}
