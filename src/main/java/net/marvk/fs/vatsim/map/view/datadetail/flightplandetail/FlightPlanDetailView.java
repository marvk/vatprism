package net.marvk.fs.vatsim.map.view.datadetail.flightplandetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.data.FlightType;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;

import java.util.List;
import java.util.stream.Collectors;

public class FlightPlanDetailView extends DataDetailSubView<FlightPlanDetailViewModel, FlightPlan> {
    @FXML
    private VBox container;
    @FXML
    private HBox noFlightPlan;
    @FXML
    private VBox content;
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

    private void setFlightPlanPanes(final Boolean newValue) {
        final ObservableList<Node> children = container.getChildren();

        if (newValue) {
            children.setAll(content);
        } else {
            children.setAll(noFlightPlan);
        }
    }

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
        // TODO flight plan available not updating
        flightRules.textProperty().bind(flightPlan.flightTypeProperty().asString());
        aircraftType.textProperty().bind(flightPlan.aircraftProperty());
        aircraftType.setTooltip(createTooltip(aircraftType.textProperty(), Duration.millis(500)));
        trueAirSpeed.textProperty().bind(stringToString(flightPlan.trueCruiseAirspeedProperty(), "kts"));
        cruiseAltitude.textProperty().bind(stringToString(flightPlan.altitudeProperty(), "ft"));
        bindToAirport(departure, flightPlan.departureAirportProperty());
        bindToAirport(arrival, flightPlan.arrivalAirportProperty());
        path.textProperty().bind(flightPlan.plannedRouteProperty());
        remarks.textProperty().bind(flightPlan.remarksProperty());
        setFlightPlanPanes(isFlightPlanAvailable(flightPlan));
    }

    private static boolean isFlightPlanAvailable(final FlightPlan flightPlan) {
        final FlightType flightType = flightPlan.flightTypeProperty().get();
        return flightType != FlightType.UNKNOWN && flightType != null;
    }

    @Override
    protected void clear(final FlightPlan oldValue) {
        super.clear(oldValue);
        setFlightPlanPanes(false);
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
    protected void setToDeparture(final MouseEvent event) {
        viewModel.setToArrival();
    }

    @FXML
    private void setToArrival(final MouseEvent event) {
        viewModel.setToDeparture();
    }
}
