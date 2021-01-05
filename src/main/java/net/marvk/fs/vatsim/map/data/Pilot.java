package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import lombok.Value;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.api.data.VatsimPilot;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pilot extends Client implements Data {
    private static final Pattern FLIGHT_NUMBER_PARSER = Pattern.compile("^(?<icao>[A-Z]{3})(?<number>[0-9][A-Z0-9]*)$");
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

    private final ObjectProperty<Eta> eta = new SimpleObjectProperty<>(Eta.UNKNOWN);

    private final ReadOnlyListWrapper<FlightInformationRegionBoundary> firbs =
            RelationshipReadOnlyListWrapper.withOtherList(this, FlightInformationRegionBoundary::pilots);

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

        parseAirlineAndFlightNumber(client.getCallsign());
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

        eta.set(calculateEta());
    }

    private Eta calculateEta() {
        if (getPosition() == null) {
            return Eta.UNKNOWN;
        } else {
            final Double distanceToDeparture = dist(flightPlan.getDepartureAirport());
            final Double distanceToArrival = dist(flightPlan.getArrivalAirport());

            if (groundSpeed.get() < 50) {
                if (distanceToDeparture != null && distanceToDeparture < 8000) {
                    return Eta.DEPARTING;
                } else if (distanceToArrival != null && distanceToArrival < 8000) {
                    return Eta.ARRIVING;
                } else {
                    return Eta.UNKNOWN;
                }
            } else if (distanceToArrival != null) {
                return new Eta(GeomUtil.duration(distanceToArrival, groundSpeed.get()));
            } else {
                return Eta.UNKNOWN;
            }
        }
    }

    private Double dist(final Airport airport) {
        return airport == null ? null : GeomUtil.distanceOnMsl(getPosition(), airport.getPosition());
    }

    private void parseAirlineAndFlightNumber(final String callsign) {
        final Matcher matcher = FLIGHT_NUMBER_PARSER.matcher(callsign);

        if (matcher.matches()) {
            final String icao = matcher.group("icao");
            final String number = matcher.group("number");

            airline.set(new Airline(icao));
            flightNumber.set(number);
        } else {
            airline.set(null);
            flightNumber.set(null);
        }
    }

    @Override
    public ClientType clientType() {
        return ClientType.PILOT;
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

    public ReadOnlyObjectProperty<Airline> airlineProperty() {
        return airline;
    }

    public String getFlightNumber() {
        return flightNumber.get();
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

    @Value
    public static class Eta {
        public static final Eta ARRIVING = new Eta(Duration.ZERO);
        public static final Eta DEPARTING = new Eta(ChronoUnit.ERAS.getDuration());
        public static final Eta UNKNOWN = new Eta(ChronoUnit.FOREVER.getDuration());

        Duration duration;

        public boolean isDeparting() {
            return DEPARTING.equals(this);
        }

        public boolean isArriving() {
            return ARRIVING.equals(this);
        }

        public boolean isUnknown() {
            return UNKNOWN.equals(this);
        }

        public boolean isEnRoute() {
            return !isDeparting() && !isArriving() && !isUnknown();
        }

        public Duration getDuration() {
            return duration;
        }
    }
}
