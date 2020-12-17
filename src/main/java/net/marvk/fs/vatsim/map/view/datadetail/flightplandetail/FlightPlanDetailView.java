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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.data.FlightType;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;

import java.util.List;

public class FlightPlanDetailView extends DataDetailSubView<FlightPlanDetailViewModel, FlightPlan> {
    @FXML
    private Label departureName;
    @FXML
    private Label arrivalName;
    @FXML
    private Label alternateName;

    @FXML
    private Label departureIcao;
    @FXML
    private Label arrivalIcao;
    @FXML
    private Label alternateIcao;

    @FXML
    private Label flightRules;
    @FXML
    private Label aircraftType;
    @FXML
    private Label trueAirSpeed;
    @FXML
    private Label cruiseAltitude;
    @FXML
    private TextArea path;
    @FXML
    private TextArea remarks;
    @FXML
    private VBox container;
    @FXML
    private HBox noFlightPlan;
    @FXML
    private GridPane content;

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
                departureIcao,
                arrivalIcao,
                alternateIcao,
                departureName,
                arrivalName,
                alternateName
        );
    }

    @Override
    public void initialize() {
        super.initialize();
        path.setOnMouseClicked(container::fireEvent);
    }

    private void setFlightPlanPanes(final Boolean newValue) {
        final ObservableList<Node> children = container.getChildren();

        if (newValue) {
            children.setAll(content);
        } else {
            children.setAll(noFlightPlan);
        }
    }

    @Override
    protected void setData(final FlightPlan flightPlan) {
        // TODO flight plan available not updating
        flightRules.textProperty().bind(flightPlan.flightTypeProperty().asString());
        aircraftType.textProperty().bind(flightPlan.aircraftProperty());
        aircraftType.setTooltip(createTooltip(aircraftType.textProperty(), Duration.millis(500)));
        trueAirSpeed.textProperty().bind(stringToString(flightPlan.trueCruiseAirspeedProperty(), "kts"));
        cruiseAltitude.textProperty().bind(stringToString(flightPlan.altitudeProperty(), "ft"));
        bindToAirport(departureIcao, departureName, flightPlan.departureAirportProperty());
        bindToAirport(arrivalIcao, arrivalName, flightPlan.arrivalAirportProperty());
        bindToAirport(alternateIcao, alternateName, flightPlan.alternativeAirportProperty());
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

    private void bindToAirport(final Label icao, final Label name, final ReadOnlyObjectProperty<Airport> airportProperty) {
        icao.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    if (airportProperty.get() == null) {
                        return "";
                    }

                    return airportProperty.get().getIcao();
                },
                airportProperty
        ));
        icao.setOnMouseClicked(e -> viewModel.setDataDetail(airportProperty.get()));
        name.textProperty().bind(Bindings.createStringBinding(
                () -> {
                    if (airportProperty.get() == null) {
                        return "";
                    }

                    return airportProperty.get().getNames().get(0).get();
                },
                airportProperty
        ));
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
}
