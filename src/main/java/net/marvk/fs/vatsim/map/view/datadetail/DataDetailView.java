package net.marvk.fs.vatsim.map.view.datadetail;

import de.saxsys.mvvmfx.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.datadetail.airportdetail.AirportDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.airportdetail.AirportDetailViewModel;
import net.marvk.fs.vatsim.map.view.datadetail.controllerdetail.ControllerDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.controllerdetail.ControllerDetailViewModel;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;
import net.marvk.fs.vatsim.map.view.datadetail.flightinformationregionboundarydetail.FlightInformationRegionBoundaryDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.pilotdetail.PilotDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.upperinformationregiondetail.UpperInformationRegionDetailView;

import java.util.Optional;

public class DataDetailView implements FxmlView<DataDetailViewModel> {
    @FXML
    private Label type;

    @FXML
    private ToggleButton follow;

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

    private final NameVisitor nameVisitor = new NameVisitor();

    private PaneManager paneManager;

    public void initialize() {
        paneManager = new PaneManager(context);

        viewModel.dataProperty().addListener((observable, oldValue, newValue) -> {
            setPane(paneManager.visit(newValue));
            type.setText(nameVisitor.visit(newValue));
        });

        historyBack.disableProperty().bind(viewModel.historyBackAvailableProperty().not());
        historyForward.disableProperty().bind(viewModel.historyForwardAvailableProperty().not());

        container.setOnKeyPressed(this::handleKeyEvent);

        container.setOnMouseClicked(this::handleMousePressed);
    }

    private void handleMousePressed(final MouseEvent event) {
        // TODO

        if (event.getButton() == MouseButton.FORWARD) {
            historyForward(null);
        } else if (event.getButton() == MouseButton.BACK) {
            historyBack(null);
        }
    }

    private void handleKeyEvent(final KeyEvent event) {
        // TODO not working
//        switch (event.getCode()) {
//            case RIGHT -> viewModel.historyForward();
//            case LEFT -> viewModel.historyBack();
//        }
    }

    private void setPane(final Optional<Parent> maybePane) {
        container.getChildren().clear();
        maybePane.ifPresent(e -> container.getChildren().add(e));
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

    public void toggleFollow(final ActionEvent actionEvent) {
        viewModel.setFollow(follow.isSelected());
    }

    private static class PaneManager implements OptionalDataVisitor<Parent> {
        private final ViewTuple<PilotDetailView, DataDetailSubViewModel<Pilot>> pilotDetailView;

        private final ViewTuple<AirportDetailView, AirportDetailViewModel> airportDetailView;

        private final ViewTuple<FlightInformationRegionBoundaryDetailView, DataDetailSubViewModel<FlightInformationRegionBoundary>> firbDetailView;

        private final ViewTuple<UpperInformationRegionDetailView, DataDetailSubViewModel<UpperInformationRegion>> uirDetailView;

        private final ViewTuple<ControllerDetailView, ControllerDetailViewModel> controllerView;

        public PaneManager(final Context context) {
            this.pilotDetailView = FluentViewLoader
                    .fxmlView(PilotDetailView.class)
                    .context(context)
                    .load();

            this.airportDetailView = FluentViewLoader
                    .fxmlView(AirportDetailView.class)
                    .context(context)
                    .load();

            this.firbDetailView = FluentViewLoader
                    .fxmlView(FlightInformationRegionBoundaryDetailView.class)
                    .context(context)
                    .load();

            this.uirDetailView = FluentViewLoader
                    .fxmlView(UpperInformationRegionDetailView.class)
                    .context(context)
                    .load();

            this.controllerView = FluentViewLoader
                    .fxmlView(ControllerDetailView.class)
                    .context(context)
                    .load();
        }

        @Override
        public Optional<Parent> visit(final Controller controller) {
            controllerView.getViewModel().setData(controller);
            return Optional.of(controllerView.getView());
        }

        @Override
        public Optional<Parent> visit(final UpperInformationRegion upperInformationRegion) {
            uirDetailView.getViewModel().setData(upperInformationRegion);
            return Optional.of(uirDetailView.getView());
        }

        @Override
        public Optional<Parent> visit(final Airport airport) {
            airportDetailView.getViewModel().setData(airport);
            return Optional.of(airportDetailView.getView());
        }

        @Override
        public Optional<Parent> visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            firbDetailView.getViewModel().setData(flightInformationRegionBoundary);
            return Optional.of(firbDetailView.getView());
        }

        @Override
        public Optional<Parent> visit(final Pilot pilot) {
            pilotDetailView.getViewModel().setData(pilot);
            return Optional.of(pilotDetailView.getView());
        }
    }
}
