package net.marvk.fs.vatsim.map.view.clientdetail;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import net.marvk.fs.vatsim.map.data.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

public class ClientDetailView implements FxmlView<ClientDetailViewModel> {
    @FXML
    private Label altitude;
    @FXML
    private TextArea remarks;
    @FXML
    private Label cid;
    @FXML
    private Label onlineSince;
    @FXML
    private Label server;
    @FXML
    private Label latitude;
    @FXML
    private Label heading;
    @FXML
    private Label longitude;
    @FXML
    private Label groundSpeed;
    @FXML
    private Label qnhMillibars;
    @FXML
    private Label qnhInchesMercury;
    @FXML
    private Label squawk;
    @FXML
    private TextArea flightPlan;
    @FXML
    private Label name;
    @FXML
    private TextArea atis;
    @FXML
    private TitledPane atc;
    @FXML
    private TitledPane pilot;
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
        final ClientViewModel client = viewModel.getClient();

        client.rawClientTypeProperty().addListener((observable, oldValue, newValue) -> setChildren(newValue));
        setChildren(client.rawClientTypeProperty().get());

        callsign.textProperty().bind(client.callsignProperty());
        name.textProperty().bind(client.realNameProperty());
        cid.textProperty().bind(client.cidProperty());

        final FlightPlanViewModel flightPlan = client.flightPlan();

        flightRules.textProperty().bind(flightPlan.rawFlightTypeProperty().asString());
        aircraftType.textProperty().bind(flightPlan.aircraftProperty());
        trueAirSpeed.textProperty().bind(nullSafeStringBindingFromNumber(client.clientStatus().groundSpeed(),
                Number::intValue
        ));
        cruiseAltitude.textProperty().bind(flightPlan.altitudePropertyProperty());
        departure.textProperty().bind(flightPlan.departureAirportProperty());
        arrival.textProperty().bind(flightPlan.destinationAirportProperty());

        final ControllerDataViewModel controllerData = client.controllerData();

        final AirportViewModel airport = controllerData.airport();
        this.airport.textProperty().bind(airport.nameProperty());
        this.airport.tooltipProperty().bind(Bindings.createObjectBinding(
                () -> new Tooltip(airport.icaoProperty().get()),
                airport.icaoProperty()
        ));

        final FlightInformationRegionViewModel fir = controllerData.flightInformationRegion();
        this.fir.textProperty().bind(fir.nameProperty());
        this.fir.tooltipProperty().bind(Bindings.createObjectBinding(
                () -> new Tooltip(fir.icaoProperty().get()),
                fir.icaoProperty()
        ));

        final UpperInformationRegionViewModel uir = controllerData.upperInformationRegion();
        this.uir.textProperty().bind(uir.nameProperty());
        this.uir.tooltipProperty().bind(Bindings.createObjectBinding(
                () -> new Tooltip(uir.icaoProperty().get()),
                uir.icaoProperty()
        ));

        this.atis.textProperty().bind(nullSafeStringBinding(client.atisMessageProperty(),
                e -> e.replaceAll("\\^§", "\n")
        ));

        this.flightPlan.textProperty().bind(flightPlan.routeProperty());

        this.remarks.textProperty().bind(flightPlan.remarksProperty());

        this.latitude.textProperty().bind(nullSafeStringBinding(
                client.clientStatus().position(),
                Point2D::getX
        ));
        this.longitude.textProperty().bind(nullSafeStringBinding(
                client.clientStatus().position(),
                Point2D::getY
        ));

        this.groundSpeed.textProperty().bind(nullSafeStringBindingFromNumber(client.clientStatus().groundSpeed(),
                Number::intValue
        ));
        this.heading.textProperty().bind(client.clientStatus().heading().asString());
        this.altitude.textProperty().bind(client.clientStatus().altitude().asString());
        this.squawk.textProperty().bind(client.transponderProperty());
        this.qnhInchesMercury.textProperty().bind(client.qnhInchesMercuryProperty().concat("inHg"));
        this.qnhMillibars.textProperty().bind(client.qnhMillibarsProperty().concat("mbar"));

        this.onlineSince.textProperty().bind(nullSafeStringBinding(client.logonTimeProperty(),
                e -> {
                    final Duration duration = Duration.between(e, LocalDateTime.now().atOffset(ZoneOffset.UTC));
                    return duration.toHoursPart() + ":" + duration.toMinutesPart();
                }
        ));
        this.onlineSince.tooltipProperty().bind(nullSafeObjectBinding(client.logonTimeProperty(),
                e -> new Tooltip(e.toString())

        ));

        this.server.textProperty().bind(client.serverProperty());

    }

    private static <T, O> ObjectBinding<O> nullSafeObjectBinding(
            final ObservableValue<T> property,
            final Function<T, O> extractor
    ) {
        return nullSafeObjectBinding(property, extractor, property);
    }

    private static <T, O> ObjectBinding<O> nullSafeObjectBinding(
            final ObservableValue<T> property,
            final Function<T, O> extractor,
            final Observable... dependencies
    ) {
        return Bindings.createObjectBinding(() -> {
            if (property.getValue() == null) {
                return null;
            }

            return extractor.apply(property.getValue());
        }, dependencies);
    }

    private static <T> StringBinding nullSafeStringBinding(
            final ObservableValue<T> property,
            final Function<T, ?> extractor
    ) {
        return nullSafeStringBinding(property, extractor, property);
    }

    private static <T> StringBinding nullSafeStringBinding(
            final ObservableValue<T> property,
            final Function<T, ?> extractor,
            final Observable... dependencies
    ) {
        return Bindings.createStringBinding(() -> {
            if (property.getValue() == null) {
                return null;
            }

            return extractor.apply(property.getValue()).toString();
        }, dependencies);
    }

    private static StringBinding nullSafeStringBindingFromNumber(
            final ObservableNumberValue property,
            final Function<Number, ?> extractor
    ) {
        return nullSafeStringBindingFromNumber(property, extractor, property);
    }

    private static StringBinding nullSafeStringBindingFromNumber(
            final ObservableNumberValue property,
            final Function<Number, ?> extractor,
            final Observable... dependencies
    ) {
        return Bindings.createStringBinding(() -> {
            if (property.getValue() == null) {
                return null;
            }

            return extractor.apply(property.getValue()).toString();
        }, dependencies);
    }

    private void setChildren(final RawClientType newValue) {
        final ObservableList<Node> children = root.getChildren();
        if (newValue == RawClientType.PILOT) {
            children.remove(atc);
            children.add(1, pilot);
        } else if (newValue == RawClientType.ATC) {
            children.remove(pilot);
            children.add(1, atc);
        } else {
            children.remove(atc);
            children.remove(pilot);
        }
    }
}
