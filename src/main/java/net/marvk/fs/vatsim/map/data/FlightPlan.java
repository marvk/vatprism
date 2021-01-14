package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.data.VatsimFlightPlan;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class FlightPlan implements Settable<VatsimFlightPlan>, Data {
    private static final Pattern ALTITUDE_PATTERN = Pattern.compile("^(?<prefix>FL|F)?(?<amount>\\d+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern AIRCRAFT_PATTERN = Pattern.compile("^(?:./)?(....?)(?:/.*)?$", Pattern.CASE_INSENSITIVE);
    private final Pilot pilot;

    private final StringProperty aircraft = new SimpleStringProperty();
    private final StringProperty aircraftShort = new SimpleStringProperty();
    private final ObjectProperty<FlightRule> flightRule = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> departureTime = new SimpleObjectProperty<>();
    private final StringProperty altitude = new SimpleStringProperty();
    private final StringProperty trueCruiseAirspeed = new SimpleStringProperty();
    private final ObjectProperty<Duration> enrouteProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Duration> fuelProperty = new SimpleObjectProperty<>();

    private final DoubleProperty totalDistance = new SimpleDoubleProperty(Double.NaN);

    private final StringProperty plannedRoute = new SimpleStringProperty();
    private final StringProperty remarks = new SimpleStringProperty();

    private final ReadOnlyObjectWrapper<Airport> departureAirport = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getDepartingWritable);
    private final ReadOnlyObjectWrapper<Airport> arrivalAirport = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getArrivingWritable);
    private final ReadOnlyObjectWrapper<Airport> alternativeAirport = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getAlternativesWritable);

    public FlightPlan(final Pilot pilot) {
        this.pilot = pilot;
    }

    @Override
    public void setFromModel(final VatsimFlightPlan model) {
        if (model == null) {
            return;
        }

        aircraft.set(model.getAircraft());
        aircraftShort.set(shortAircraft(model.getAircraft()));
        flightRule.set(FlightRule.fromString(model.getFlightRules()));
        departureTime.set(parseDepartureTime(model.getDepartureTime()));
        altitude.set(parseAltitude(model.getAltitude()));
        trueCruiseAirspeed.set(model.getCruiseTas());
        enrouteProperty.set(parseDuration(model.getEnrouteTime()));
        fuelProperty.set(parseDuration(model.getFuelTime()));

        plannedRoute.set(model.getRoute());
        remarks.set(model.getRemarks());

        departureAirport.addListener((observable, oldValue, newValue) -> setTotalDistance());
        arrivalAirport.addListener((observable, oldValue, newValue) -> setTotalDistance());
    }

    private String shortAircraft(final String aircraft) {
        final Matcher matcher = AIRCRAFT_PATTERN.matcher(aircraft);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            log.warn("Failed to parse short aircraft type from string \"%s\"".formatted(aircraft));
            return null;
        }
    }

    private void setTotalDistance() {
        final Airport departureAirport = this.departureAirport.get();
        final Airport arrivalAirport = this.arrivalAirport.get();

        if (departureAirport != null && arrivalAirport != null) {
            totalDistance.set(GeomUtil.distanceOnMsl(departureAirport.getPosition(), arrivalAirport.getPosition()));
        } else {
            totalDistance.set(Double.NaN);
        }
    }

    public boolean isSet() {
        return getFlightRule() != FlightRule.UNKNOWN && getFlightRule() != null;
    }

    public Pilot getPilot() {
        return pilot;
    }

    public String getAircraft() {
        return aircraft.get();
    }

    public ReadOnlyStringProperty aircraftProperty() {
        return aircraft;
    }

    public String getAircraftShort() {
        return aircraftShort.get();
    }

    public ReadOnlyStringProperty aircraftShortProperty() {
        return aircraftShort;
    }

    public FlightRule getFlightRule() {
        return flightRule.get();
    }

    public ReadOnlyObjectProperty<FlightRule> flightRuleProperty() {
        return flightRule;
    }

    public LocalTime getDepartureTime() {
        return departureTime.get();
    }

    public ReadOnlyObjectProperty<LocalTime> departureTimeProperty() {
        return departureTime;
    }

    public String getAltitude() {
        return altitude.get();
    }

    public ReadOnlyStringProperty altitudeProperty() {
        return altitude;
    }

    public String getTrueCruiseAirspeed() {
        return trueCruiseAirspeed.get();
    }

    public ReadOnlyStringProperty trueCruiseAirspeedProperty() {
        return trueCruiseAirspeed;
    }

    public Duration getEnrouteProperty() {
        return enrouteProperty.get();
    }

    public ReadOnlyObjectProperty<Duration> enroutePropertyProperty() {
        return enrouteProperty;
    }

    public Duration getFuelProperty() {
        return fuelProperty.get();
    }

    public ReadOnlyObjectProperty<Duration> fuelPropertyProperty() {
        return fuelProperty;
    }

    public String getPlannedRoute() {
        return plannedRoute.get();
    }

    public ReadOnlyStringProperty plannedRouteProperty() {
        return plannedRoute;
    }

    public String getRemarks() {
        return remarks.get();
    }

    public ReadOnlyStringProperty remarksProperty() {
        return remarks;
    }

    public Airport getDepartureAirport() {
        return departureAirport.get();
    }

    ObjectProperty<Airport> departureAirportPropertyWritable() {
        return departureAirport;
    }

    public ReadOnlyObjectProperty<Airport> departureAirportProperty() {
        return departureAirport.getReadOnlyProperty();
    }

    public Airport getArrivalAirport() {
        return arrivalAirport.get();
    }

    ObjectProperty<Airport> arrivalAirportPropertyWritable() {
        return arrivalAirport;
    }

    public ReadOnlyObjectProperty<Airport> arrivalAirportProperty() {
        return arrivalAirport.getReadOnlyProperty();
    }

    public Airport getAlternativeAirport() {
        return alternativeAirport.get();
    }

    ObjectProperty<Airport> alternativeAirportPropertyWritable() {
        return alternativeAirport;
    }

    public ReadOnlyObjectProperty<Airport> alternativeAirportProperty() {
        return alternativeAirport.getReadOnlyProperty();
    }

    public double getTotalDistance() {
        return totalDistance.get();
    }

    public ReadOnlyDoubleProperty totalDistanceProperty() {
        return totalDistance;
    }

    public boolean isDomestic() {
        return isDepartureAndArrivalPresent() && isDomesticNullVulnerable();
    }

    public boolean isInternational() {
        return isDepartureAndArrivalPresent() && !isDomesticNullVulnerable();
    }

    private boolean isDomesticNullVulnerable() {
        return getDepartureAirport().getCountry().equals(getArrivalAirport().getCountry());
    }

    private boolean isDepartureAndArrivalPresent() {
        return getDepartureAirport() != null && getArrivalAirport() != null;
    }

    private static Duration parseDuration(final String hoursMinutes) {
        return ParseUtil.parseNullSafe(
                hoursMinutes,
                (hm) -> Duration.ofHours(Integer.parseInt(hoursMinutes.substring(0, 2)))
                                .plusMinutes(Integer.parseInt(hoursMinutes.substring(2, 4)))
        );
    }

    private static String parseAltitude(final String altitude) {
        return ParseUtil.parseNullSafe(altitude, s -> {
            final Matcher matcher = ALTITUDE_PATTERN.matcher(altitude);
            if (matcher.matches()) {
                final String prefix = matcher.group("prefix");
                final String amount = matcher.group("amount");

                final int multiplier = prefix == null ? 1 : 100;

                return String.valueOf(Integer.parseInt(amount) * multiplier);
            }
            return null;
        });
    }

    private static LocalTime parseDepartureTime(final String string) {
        return ParseUtil.parseNullSafe(string, s -> {
            try {
                if (s.length() > 4) {
                    log.info("Identified invalid time \"%s\"".formatted(s));
                    return null;
                }

                // min because users may input more than four numbers
                final String time = "0".repeat(4 - s.length()) + s;

                final String hourString = time.substring(0, 2);
                final String minuteString = time.substring(2, 4);

                return LocalTime.of(
                        "24".equals(hourString) ? 0 : Integer.parseInt(hourString),
                        "60".equals(minuteString) ? 0 : Integer.parseInt(minuteString)
                );
            } catch (final DateTimeException | NumberFormatException e) {
                log.warn("Failed to parse time \"%s\"".formatted(string));
                return null;
            }
        });
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
