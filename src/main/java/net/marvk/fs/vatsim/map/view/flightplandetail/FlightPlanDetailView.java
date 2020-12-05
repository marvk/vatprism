package net.marvk.fs.vatsim.map.view.flightplandetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubView;

import java.util.List;
import java.util.stream.Collectors;

public class FlightPlanDetailView extends DataDetailSubView<FlightPlanDetailViewModel, FlightPlan> {
    @FXML
    private TitledPane flightPlan;
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
    private TextArea path;
    @FXML
    private TextArea remarks;

    @Override
    protected List<TextArea> textAreas() {
        return List.of(
                path,
                remarks
        );
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                flightRules,
                aircraftType,
                trueAirSpeed,
                cruiseAltitude,
                departure,
                arrival
        );
    }

    @Override
    protected void setData(final FlightPlan flightPlan) {
        flightRules.textProperty().bind(flightPlan.flightTypeProperty().asString());
        aircraftType.textProperty().bind(flightPlan.aircraftProperty());
        aircraftType.setTooltip(createTooltip(aircraftType.textProperty(), Duration.millis(500)));
        trueAirSpeed.textProperty().bind(flightPlan.trueCruiseAirspeedProperty());
        cruiseAltitude.textProperty().bind(flightPlan.altitudeProperty());
        bindToAirport(departure, flightPlan.departureAirportProperty());
        bindToAirport(arrival, flightPlan.arrivalAirportProperty());
        path.textProperty().bind(flightPlan.plannedRouteProperty());
        remarks.textProperty().bind(flightPlan.remarksProperty());
    }

    private static void bindToAirport(final Label label, final ReadOnlyObjectProperty<Airport> airportProperty) {
        label.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    if (airportProperty.get() == null) {
                        return "";
                    }

                    return airportProperty.get().getIcao();
                },
                airportProperty
        ));
        final Tooltip tooltip = createTooltip(Bindings.createStringBinding(
                () -> {
                    if (airportProperty.get() == null) {
                        return "";
                    }
                    return airportProperty.get().getNames().stream().collect(Collectors.joining("\n"));
                },
                airportProperty
        ));
        label.setTooltip(tooltip);
    }

    private static Tooltip createTooltip(final ObservableValue<String> textProperty) {
        return createTooltip(textProperty, Duration.ZERO);
    }

    private static Tooltip createTooltip(final ObservableValue<String> textProperty, final Duration duration) {
        final var tooltip = new Tooltip();
        tooltip.setShowDelay(duration);
        tooltip.textProperty().bind(textProperty);
        return tooltip;
    }

    @FXML
    protected void goToDeparture(final MouseEvent event) {
        viewModel.goToDeparture();
    }

    @FXML
    private void goToArrival(final MouseEvent event) {
        viewModel.goToArrival();
    }
}
