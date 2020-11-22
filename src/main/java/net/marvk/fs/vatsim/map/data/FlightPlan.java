package net.marvk.fs.vatsim.map.data;

import lombok.Data;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class FlightPlan {
    private static final Pattern ALTITUDE_PATTERN = Pattern.compile("^(?<prefix>FL|F)?(?<amount>\\d+)$", Pattern.CASE_INSENSITIVE);
    private final Integer revision;
    private final String aircraft;
    private final FlightType flightType;

    private final LocalTime departureTime;
    private final String departureAirport;
    private final Point departureAirportPosition;

    private final String destinationAirport;
    private final Point destinationAirportPosition;

    private final String alternativeAirport;

    private final Integer altitude;
    private final Integer trueAirspeedCruise;

    private final Duration enroute;
    private final Duration fuel;

    private final String route;
    private final String remarks;

    public static FlightPlan fromVatsimClient(final VatsimClient vatsimClient) {
        return new FlightPlan(
                ParseUtil.parseNullSafe(vatsimClient.getPlannedRevision(), Integer::parseInt),
                vatsimClient.getPlannedAircraft(),
                FlightType.fromString(vatsimClient.getPlannedFlightType()),
                parseDepartureTime(vatsimClient.getPlannedDepartureTime()),
                vatsimClient.getPlannedDepartureAirport(),
                Point.from(vatsimClient.getPlannedDepartureAirportLongitude(), vatsimClient.getPlannedDepartureAirportLatitude()),
                vatsimClient.getPlannedDestinationAirport(),
                Point.from(vatsimClient.getPlannedArrivalAirportLongitude(), vatsimClient.getPlannedArrivalAirportLatitude()),
                vatsimClient.getPlannedAlternativeAirport(),
                parseAltitude(vatsimClient.getPlannedAltitude()),
                ParseUtil.parseNullSafe(vatsimClient.getPlannedTrueAirspeedCruise(), Integer::parseInt),
                parseDuration(vatsimClient.getPlannedHoursEnroute(), vatsimClient.getPlannedMinutesEnroute()),
                parseDuration(vatsimClient.getPlannedHoursFuel(), vatsimClient.getPlannedMinutesFuel()),
                vatsimClient.getPlannedRoute(),
                vatsimClient.getPlannedRemarks()
        );
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
