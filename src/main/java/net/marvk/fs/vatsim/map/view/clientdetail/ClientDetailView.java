package net.marvk.fs.vatsim.map.view.clientdetail;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import net.marvk.fs.vatsim.map.data.FlightPlanViewModel;

public class ClientDetailView implements FxmlView<ClientDetailViewModel> {
    @FXML
    private Label callsign;
    @FXML
    private Label flightRules;
    @FXML
    private Label aircraftType;
    @FXML
    private Label trueAirSpeed;
    @FXML
    private Label cruiseAltitude;
    @FXML
    private Label departure;
    @FXML
    private Label arrival;
    @FXML
    private Pane root;
    @FXML
    private GridPane grid;
    @InjectViewModel
    private ClientDetailViewModel viewModel;

    public void initialize() {
        callsign.textProperty().bind(viewModel.getClient().callsignProperty());

        final FlightPlanViewModel flightPlan = viewModel.getClient().flightPlan();

        flightRules.textProperty().bind(flightPlan.rawFlightTypeProperty().asString());
        aircraftType.textProperty().bind(flightPlan.aircraftProperty());
        trueAirSpeed.textProperty().bind(flightPlan.trueAirspeedCruiseProperty());
        cruiseAltitude.textProperty().bind(flightPlan.altitudePropertyProperty());
        departure.textProperty().bind(flightPlan.departureAirportProperty());
        arrival.textProperty().bind(flightPlan.destinationAirportProperty());
    }

}
