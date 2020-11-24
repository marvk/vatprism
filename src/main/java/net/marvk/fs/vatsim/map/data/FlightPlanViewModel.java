package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlightPlanViewModel extends SimpleDataViewModel<VatsimClient, FlightPlanViewModel> implements ViewModel {
    private static final Pattern ALTITUDE_PATTERN = Pattern.compile("^(?<prefix>FL|F)?(?<amount>\\d+)$", Pattern.CASE_INSENSITIVE);

    public FlightPlanViewModel() {
    }

    public StringProperty revisionProperty() {
        return stringProperty("revision", VatsimClient::getPlannedRevision);
    }

    public StringProperty aircraftProperty() {
        return stringProperty("aircraft", VatsimClient::getPlannedAircraft);
    }

    public ObjectProperty<RawFlightType> rawFlightTypeProperty() {
        return objectProperty("rawFlightType", c -> RawFlightType.fromString(c.getPlannedFlightType()));
    }

    public ObjectProperty<LocalTime> departureTimeProperty() {
        return objectProperty("departureTime", c -> parseDepartureTime(c.getPlannedDepartureTime()));
    }

    public StringProperty departureAirportProperty() {
        return stringProperty("departureAirport", VatsimClient::getPlannedDepartureAirport);
    }

    public ObjectProperty<Point2D> departureAirportPositionProperty() {
        return objectProperty("departureAirportPosition", c -> GeomUtil.parsePoint(c.getPlannedDepartureAirportLongitude(), c
                .getPlannedDepartureAirportLatitude()));
    }

    public StringProperty destinationAirportProperty() {
        return stringProperty("destinationAirport", VatsimClient::getPlannedDestinationAirport);
    }

    public ObjectProperty<Point2D> destinationAirportPositionProperty() {
        return objectProperty("destinationAirportPosition", c -> GeomUtil.parsePoint(c.getPlannedArrivalAirportLongitude(), c
                .getPlannedArrivalAirportLatitude()));
    }

    public StringProperty altitudePropertyProperty() {
        return stringProperty("altitude", c -> String.valueOf(parseAltitude(c.getAltitude())));
    }

    public StringProperty trueAirspeedCruiseProperty() {
        return stringProperty("trueAirspeedCruise", VatsimClient::getPlannedTrueAirspeedCruise);
    }

    public ObjectProperty<Duration> enrouteProperty() {
        return objectProperty("enroute", c -> parseDuration(c.getPlannedHoursEnroute(), c.getPlannedMinutesEnroute()));
    }

    public ObjectProperty<Duration> fuelProperty() {
        return objectProperty("fuel", c -> parseDuration(c.getPlannedHoursFuel(), c.getPlannedMinutesFuel()));
    }

    public StringProperty routeProperty() {
        return stringProperty("route", VatsimClient::getPlannedRoute);
    }

    public StringProperty remarksProperty() {
        return stringProperty("remarks", VatsimClient::getPlannedRemarks);
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
                final String time = "0".repeat(4 - s.length()) + s;
                return LocalTime.of(
                        Integer.parseInt(time.substring(0, 2)),
                        Integer.parseInt(time.substring(2, 4))
                );
            } catch (final DateTimeException e) {
                return null;
            }
        });
    }

}
