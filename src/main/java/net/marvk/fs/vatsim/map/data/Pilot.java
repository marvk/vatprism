package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimPilot;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

public class Pilot extends Client implements Data {
    private static final ReadOnlyObjectProperty<ClientType> CLIENT_TYPE = new ImmutableObjectProperty<>(ClientType.PILOT);
    private final FlightPlan flightPlan = new FlightPlan(this);

    private final StringProperty transponder = new SimpleStringProperty();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty groundSpeed = new SimpleDoubleProperty();
    private final DoubleProperty heading = new SimpleDoubleProperty();
    private final DoubleProperty qnhInchesMercury = new SimpleDoubleProperty();
    private final DoubleProperty qnhMilliBars = new SimpleDoubleProperty();
    private final ObjectProperty<Point2D> position = new SimpleObjectProperty<>();

    private final DoubleProperty verticalSpeed = new SimpleDoubleProperty(Double.NaN);

    private final ObjectProperty<Airline> airline = new SimpleObjectProperty<>();
    private final StringProperty flightNumber = new SimpleStringProperty();
    private final BooleanProperty flightNumberAvailable = new SimpleBooleanProperty();

    private final ObjectProperty<Eta> eta = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<FlightInformationRegionBoundary> firbs =
            RelationshipReadOnlyListWrapper.withOtherList(this, FlightInformationRegionBoundary::pilots);

    private final ObservableList<Point2D> history = FXCollections.observableArrayList();
    private final ObservableList<Point2D> unmodifiableHistory = FXCollections.unmodifiableObservableList(history);

    public Pilot() {
        flightNumberAvailable.bind(airline.isNotNull().and(flightNumber.isNotNull()));
    }

    @Override
    public void setFromModel(final VatsimClient client) {
        final VatsimPilot pilot = (VatsimPilot) client;

        final ZonedDateTime previousUpdatedTime = getLastUpdatedTime();
        final double previousAltitude = getAltitude();

        super.setFromModel(client);

        flightPlan.setFromModel(((VatsimPilot) client).getFlightPlan());

        transponder.set(pilot.getTransponder());
        altitude.set(Double.parseDouble(pilot.getAltitude()));
        groundSpeed.set(Double.parseDouble(pilot.getGroundSpeed()));
        heading.set(Double.parseDouble(pilot.getHeading()));
        qnhInchesMercury.set(Double.parseDouble(pilot.getQnhInchesMercury()));
        qnhMilliBars.set(Double.parseDouble(pilot.getQnhMillibars()));
        position.set(GeomUtil.parsePoint(pilot.getLongitude(), pilot.getLatitude()));
        history.add(position.get());

        if (!Objects.equals(previousUpdatedTime, getLastUpdatedTime())) {
            if (previousUpdatedTime != null && getLastUpdatedTime() != null) {
                final double ftDiff = getAltitude() - previousAltitude;
                final Duration durationSinceLastUpdate = Duration.between(previousUpdatedTime, getLastUpdatedTime());
                final double minutesSinceLastUpdate = durationSinceLastUpdate.toSeconds() / 60.0;
                final double fpm = ftDiff / minutesSinceLastUpdate;
                verticalSpeed.set(fpm);
            } else {
                verticalSpeed.set(Double.NaN);
            }
        }

        eta.set(Eta.of(getPosition(), getGroundSpeed(), flightPlan.getDepartureAirport(), flightPlan.getArrivalAirport()));
        getUrls().setUrlsFromString(flightPlan.getRemarks());
    }

    @Override
    public ReadOnlyObjectProperty<ClientType> clientTypeProperty() {
        return CLIENT_TYPE;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }

    public String getTransponder() {
        return transponder.get();
    }

    public ReadOnlyStringProperty transponderProperty() {
        return transponder;
    }

    public double getAltitude() {
        return altitude.get();
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public double getGroundSpeed() {
        return groundSpeed.get();
    }

    public ReadOnlyDoubleProperty groundSpeedProperty() {
        return groundSpeed;
    }

    public double getHeading() {
        return heading.get();
    }

    public ReadOnlyDoubleProperty headingProperty() {
        return heading;
    }

    public double getQnhInchesMercury() {
        return qnhInchesMercury.get();
    }

    public ReadOnlyDoubleProperty qnhInchesMercuryProperty() {
        return qnhInchesMercury;
    }

    public double getQnhMilliBars() {
        return qnhMilliBars.get();
    }

    public ReadOnlyDoubleProperty qnhMilliBarsProperty() {
        return qnhMilliBars;
    }

    public Point2D getPosition() {
        return position.get();
    }

    public ReadOnlyObjectProperty<Point2D> positionProperty() {
        return position;
    }

    public Airline getAirline() {
        return airline.get();
    }

    void setAirline(final Airline airline) {
        this.airline.set(airline);
    }

    public ReadOnlyObjectProperty<Airline> airlineProperty() {
        return airline;
    }

    public String getFlightNumber() {
        return flightNumber.get();
    }

    void setFlightNumber(final String flightNumber) {
        this.flightNumber.set(flightNumber);
    }

    public ReadOnlyStringProperty flightNumberProperty() {
        return flightNumber;
    }

    public boolean isFlightNumberAvailable() {
        return flightNumberAvailable.get();
    }

    public ReadOnlyBooleanProperty flightNumberAvailableProperty() {
        return flightNumberAvailable;
    }

    public double getVerticalSpeed() {
        return verticalSpeed.get();
    }

    public ReadOnlyDoubleProperty verticalSpeedProperty() {
        return verticalSpeed;
    }

    public Eta getEta() {
        return eta.get();
    }

    public ReadOnlyObjectProperty<Eta> etaProperty() {
        return eta;
    }

    public ObservableList<Point2D> getHistory() {
        return unmodifiableHistory;
    }

    SimpleListProperty<FlightInformationRegionBoundary> flightInformationRegionBoundariesWritable() {
        return firbs;
    }

    public ReadOnlyListProperty<FlightInformationRegionBoundary> flightInformationRegionBoundaries() {
        return firbs.getReadOnlyProperty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
