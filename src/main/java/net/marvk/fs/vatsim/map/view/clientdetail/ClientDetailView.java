package net.marvk.fs.vatsim.map.view.clientdetail;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import net.marvk.fs.vatsim.map.data.AirportViewModel;
import net.marvk.fs.vatsim.map.data.ControllerDataViewModel;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.FlightPlanViewModel;

public class ClientDetailView implements FxmlView<ClientDetailViewModel> {
    @FXML
    private Label airport;
    @FXML
    private Label uir;
    @FXML
    private Label fir;
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

        final ControllerDataViewModel controllerData = viewModel.getClient().controllerData();

        final AirportViewModel airportViewModel = controllerData.airport();
        this.airport.textProperty().bind(airportViewModel.icaoProperty());

        airportViewModel.nameProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                System.out.println(newValue);
            }
        });


        final FlightInformationRegionBoundaryViewModel fir = controllerData.fir();

        this.fir.textProperty().bind(fir.icaoProperty());
        this.fir.tooltipProperty().bind(Bindings.createObjectBinding(
                () -> new Tooltip(
                        fir.icaoProperty() +
                                (fir.oceanicProperty().get() ? " Oceanic" : "") +
                                (fir.extensionProperty().get() ? " Extension" : "")
                ),
                fir.icaoProperty(),
                fir.centerPositionProperty(),
                fir.oceanicProperty()
        ));

    }

}
