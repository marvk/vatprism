package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;
import lombok.Value;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

@Value
public class Eta {
    private static final Eta GROUND = new Eta(ChronoUnit.FOREVER.getDuration(), Status.GROUND);
    private static final Eta ARRIVING = new Eta(Duration.ZERO.minus(Duration.ofDays(365)), Status.GROUND, Status.ARRIVING);
    private static final Eta DEPARTING = new Eta(ChronoUnit.FOREVER.getDuration()
                                                                   .minus(Duration.ofSeconds(10)), Status.GROUND, Status.DEPARTING);
    private static final Eta UNKNOWN = new Eta(ChronoUnit.FOREVER.getDuration().negated(), Status.UNKNOWN);
    private static final Eta MID_AIR = new Eta(ChronoUnit.FOREVER.getDuration()
                                                                 .minus(Duration.ofSeconds(20)), Status.MID_AIR);

    Duration duration;
    EnumSet<Status> statuses;

    private Eta(final Duration duration, final Status first, final Status... rest) {
        this.duration = duration;
        this.statuses = EnumSet.of(first, rest);
    }

    public boolean is(final Status status) {
        return statuses.contains(status);
    }

    public static Eta of(final Point2D position, final double groundSpeedInKnots, final Airport departure, final Airport arrival) {
        if (position == null) {
            return UNKNOWN;
        } else {
            final Double distanceToDeparture = dist(position, departure);
            final Double distanceToArrival = dist(position, arrival);

            if (groundSpeedInKnots < 50) {
                if (distanceToDeparture != null && distanceToDeparture < 8000) {
                    return DEPARTING;
                } else if (distanceToArrival != null && distanceToArrival < 8000) {
                    return ARRIVING;
                } else {
                    return GROUND;
                }
            } else if (distanceToArrival != null) {
                return new Eta(GeomUtil.duration(distanceToArrival, groundSpeedInKnots), Status.MID_AIR, Status.EN_ROUTE);
            } else {
                return MID_AIR;
            }
        }
    }

    private static Double dist(final Point2D point, final Airport airport) {
        return airport == null ? null : GeomUtil.distanceOnMsl(point, airport.getPosition());
    }

    public enum Status {
        UNKNOWN, GROUND, MID_AIR, DEPARTING, ARRIVING, EN_ROUTE;
    }
}
