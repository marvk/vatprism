package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import lombok.extern.slf4j.Slf4j;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FlightPlan implements Settable<VatsimClient> {
    private static final Pattern ALTITUDE_PATTERN = Pattern.compile("^(?<prefix>FL|F)?(?<amount>\\d+)$", Pattern.CASE_INSENSITIVE);

    private final Pilot pilot;

    private final StringProperty aircraft = new SimpleStringProperty();
    private final ObjectProperty<FlightType> flightType = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalTime> departureTime = new SimpleObjectProperty<>();
    private final StringProperty altitude = new SimpleStringProperty();
    private final StringProperty trueCruiseAirspeed = new SimpleStringProperty();
    private final ObjectProperty<Duration> enrouteProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Duration> fuelProperty = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<Airport> departureAirport = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getDepartingWritable);
    private final ReadOnlyObjectWrapper<Airport> arrivalAirport = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getArrivingWritable);
    private final ReadOnlyObjectWrapper<Airport> alternativeAirport = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getAlternativesWritable);

    public FlightPlan(final Pilot pilot) {
        this.pilot = pilot;
    }

    @Override
    public void setFromModel(final VatsimClient model) {
        Objects.requireNonNull(model);

        aircraft.set(model.getPlannedAircraft());
        flightType.set(FlightType.fromString(model.getPlannedFlightType()));
        departureTime.set(parseDepartureTime(model.getPlannedDepartureTime()));
        altitude.set(model.getPlannedAltitude());
        trueCruiseAirspeed.set(model.getPlannedTrueAirspeedCruise());
        enrouteProperty.set(parseDuration(model.getPlannedHoursEnroute(), model.getPlannedMinutesEnroute()));
        fuelProperty.set(parseDuration(model.getPlannedHoursFuel(), model.getPlannedMinutesFuel()));
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

    public FlightType getFlightType() {
        return flightType.get();
    }

    public ReadOnlyObjectProperty<FlightType> flightTypeProperty() {
        return flightType;
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

    private static Duration parseDuration(final String hours, final String minutes) {
        return ParseUtil.parseNullSafe(
                hours,
                minutes,
                (h, m) -> Duration.ofMinutes(Integer.parseInt(m)).plusHours(Integer.parseInt(h))
        );
    }

    private static Integer parseAltitude(final String altitude) {
        return ParseUtil.parseNullSafe(altitude, s -> {
            final Matcher matcher = ALTITUDE_PATTERN.matcher(altitude);
            if (matcher.matches()) {
                final String prefix = matcher.group("prefix");
                final String amount = matcher.group("amount");

                final int multiplier = prefix == null ? 1 : 100;

                return Integer.parseInt(amount) * multiplier;
            }
            return null;
        });
    }

    private static LocalTime parseDepartureTime(final String string) {
        return ParseUtil.parseNullSafe(string, s -> {
            try {
                // min because users may input more than four numbers
                final String time = "0".repeat(4 - Math.min(s.length(), 4)) + s;
                return LocalTime.of(
                        Integer.parseInt(time.substring(0, 2)),
                        Integer.parseInt(time.substring(2, 4))
                );
            } catch (final DateTimeException | NumberFormatException e) {
                log.warn("Failed to parse time " + string, e);
                return null;
            }
        });
    }
}
