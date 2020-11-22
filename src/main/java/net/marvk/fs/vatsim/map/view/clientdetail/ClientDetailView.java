package net.marvk.fs.vatsim.map.view.clientdetail;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import org.controlsfx.tools.Borders;

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
    private GridPane flightPlan;
    @InjectViewModel
    private ClientDetailViewModel viewModel;

    public void initialize() {
        callsign.textProperty().bind(viewModel.getClient().callsignProperty());
        viewModel.getClient().flightPlanProperty().addListener((observable, oldValue, newValue) -> {
            setFlightPlan(newValue);
        });

        setFlightPlan(viewModel.getClient().getFlightPlan());

        final Node flightPlan = Borders.wrap(this.flightPlan).lineBorder().title("Flight Plan").build().build();

        root.getChildren().remove(1);
        root.getChildren().add(flightPlan);
    }

    private void setFlightPlan(final FlightPlan flightPlan) {
        if (flightPlan == null) {
            return;
        }

        System.out.println(flightPlan);

        flightRules.setText(flightPlan.getFlightType().toString());
        aircraftType.setText(flightPlan.getAircraft());
        trueAirSpeed.setText(String.valueOf(flightPlan.getTrueAirspeedCruise()));
        cruiseAltitude.setText(String.valueOf(flightPlan.getAltitude()));
        departure.setText(flightPlan.getDepartureAirport());
        arrival.setText(flightPlan.getDestinationAirport());
    }
}
