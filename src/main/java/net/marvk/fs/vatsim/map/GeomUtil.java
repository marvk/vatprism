package net.marvk.fs.vatsim.map;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import net.marvk.fs.vatsim.api.data.Point;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Collection;
import java.util.Locale;

import static java.lang.Math.*;

/**
 * @see <a href="https://www.movable-type.co.uk/scripts/latlong.html">Movable Type Scripts - Calculate distance, bearing and more between Latitude/Longitude points</a>
 */
public final class GeomUtil {
    private static final int EARTH_RADIUS = 6371;
    private static final int R = EARTH_RADIUS;

    private GeomUtil() {
        throw new AssertionError("No instances of utility class " + GeomUtil.class);
    }

    private static final DecimalFormat df;

    static {
        df = new DecimalFormat("#.######");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static Point2D[] greatCirclePolyline(final Point2D origin, final Point2D destination, final Point2D[] result) {
        final int n = result.length;

        double lastX = Double.NaN;
        double shiftX = 0;
        for (int i = 0; i < n; i++) {
            final double f = (double) i / (n - 1);
            final Point2D p = pointBetween(origin, destination, f);

            final double curX = p.getX();

            // Shift past 180th for easier drawing
            if (!Double.isNaN(lastX)) {
                final double abs = abs(lastX - curX);
                if (lastX != 0 && abs > 180) {
                    shiftX = Math.signum(lastX) * 360;
                }
            }

            result[i] = shiftX == 0
                    ? p
                    : p.add(shiftX, 0);

            lastX = curX;
        }

        return result;
    }

    public static Point2D[] greatCirclePolyline(final Point2D origin, final Point2D destination) {
        return greatCirclePolyline(origin, destination, new Point2D[51]);
    }

    public static Point2D pointBetween(final Point2D origin, final Point2D destination, final double fraction) {
        return pointBetween(origin, destination, fraction, distanceInRadiansOnMsl(origin, destination));
    }

    private static Point2D pointBetween(final Point2D origin, final Point2D destination, final double fraction, final double d) {
        if (fraction < 0 || fraction > 1) {
            throw new IllegalArgumentException("fraction must be in [0, 1], was %s".formatted(fraction));
        }

        final double lon1 = toRadians(origin.getX()); // λ1
        final double lat1 = toRadians(origin.getY()); // φ1

        final double lon2 = toRadians(destination.getX()); // λ2
        final double lat2 = toRadians(destination.getY()); // φ2

        final double f = fraction;

        final double a = sin((1 - f) * d) / sin(d);
        final double b = sin(f * d) / sin(d);
        final double x = a * cos(lat1) * cos(lon1) + b * cos(lat2) * cos(lon2);
        final double y = a * cos(lat1) * sin(lon1) + b * cos(lat2) * sin(lon2);
        final double z = a * sin(lat1) + b * sin(lat2);
        final double lat = atan2(z, sqrt(x * x + y * y));
        final double lon = atan2(y, x);

        return new Point2D(toDegrees(lon), toDegrees(lat));
    }

    private static double distanceInRadiansOnMsl(final Point2D origin, final Point2D destination) {
        final double lon1 = toRadians(origin.getX()); // λ1
        final double lat1 = toRadians(origin.getY()); // φ1

        final double lon2 = toRadians(destination.getX()); // λ2
        final double lat2 = toRadians(destination.getY()); // φ2

        final double v1 = sin((lat1 - lat2) / 2);
        final double v2 = sin((lon1 - lon2) / 2);
        return 2 * asin(sqrt(v1 * v1 + cos(lat1) * cos(lat2) * v2 * v2));
    }

    public static Point2D parsePoint(final String longitude, final String latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

            return new Point2D(Double.parseDouble(longitude), Double.parseDouble(latitude));
    }

    public static String format(final Point2D point) {
        if (point == null) {
            return "";
        }

        return String.format("%s %s", formatLon(point.getY()), formatLat(point.getX()));
    }

    public static String formatLon(final double lon) {
        return df.format(Math.abs(lon)) + (lon < 0 ? "S" : "N");
    }

    public static String formatLat(final double lat) {
        return df.format(Math.abs(lat)) + (lat < 0 ? "W" : "E");
    }

    public static Point2D parsePoint(final Double longitude, final Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        return new Point2D(longitude, latitude);
    }

    /**
     * Calculate the distance in meters at MSL between two points on the globe
     *
     * @param p1 point 1
     * @param p2 point 2
     *
     * @return Distance in meters
     */
    public static double distanceOnMsl(final Point2D p1, final Point2D p2) {
        return distanceOnMslDegrees(p1.getY(), p1.getX(), p2.getY(), p2.getX());
    }

    public static double squareDistance(final double x1, final double y1, final double x2, final double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    public static double knotsToMs(final double knots) {
        return knots * 0.514444444;
    }

    public static double metersToNauticalMiles(final double meters) {
        return meters * 0.000539957;
    }

    public static Duration duration(final double meters, final double knots) {
        return Duration.ofSeconds((long) (meters / knotsToMs(knots)));
    }

    private static double distanceOnMslDegrees(
            final double lat1, final double lon1,
            final double lat2, final double lon2
    ) {
        final double phi1 = Math.toRadians(lat1);
        final double phi2 = Math.toRadians(lat2);
        final double deltaPhi = Math.toRadians(lat2 - lat1);
        final double deltaLambda = Math.toRadians(lon2 - lon1);

        final double a =
                sin(deltaPhi / 2.0) * sin(deltaPhi / 2.0) +
                        cos(phi1) * cos(phi2) * sin(deltaLambda / 2.0) * sin(deltaLambda / 2.0);

        final double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return 1000.0 * R * c;
    }

    public static Point2D parsePoint(final Point position) {
        return parsePoint(position.getX(), position.getY());
    }

    public static Point2D center(final Rectangle2D rectangle) {
        return new Point2D(rectangle.getMinX() + rectangle.getWidth() / 2.0, rectangle.getMinY() + rectangle.getHeight() / 2.0);
    }

    public static Rectangle2D boundingRect(final Collection<Rectangle2D> rectangles) {
        if (rectangles.isEmpty()) {
            return new Rectangle2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0, 0);
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;

        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (final Rectangle2D rectangle : rectangles) {
            minX = Math.min(minX, rectangle.getMinX());
            minY = Math.min(minY, rectangle.getMinY());
            maxX = Math.max(maxX, rectangle.getMaxX());
            maxY = Math.max(maxY, rectangle.getMaxY());
        }

        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }
}
