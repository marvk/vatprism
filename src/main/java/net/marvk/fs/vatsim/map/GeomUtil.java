package net.marvk.fs.vatsim.map;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import net.marvk.fs.vatsim.api.data.Point;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class GeomUtil {
    private GeomUtil() {
        throw new AssertionError("No instances of utility class " + GeomUtil.class);
    }

    private static final DecimalFormat df;

    static {
        df = new DecimalFormat("#.######");
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static List<Point2D> greatCirclePolyline(final Point2D origin, final Point2D destination, final double stepSize) {
        return greatCirclePolyline(origin.getX(), origin.getY(), destination.getX(), destination.getY(), stepSize);

    }

    private static List<Point2D> greatCirclePolyline(final double originLon, final double originLat, final double destinationLon, final double destinationLat, final double stepSize) {

        final ArrayList<Point2D> result = new ArrayList<>();

        double lon = originLon;
        double lat = originLat;

        while (Math.abs(lon - destinationLon) < stepSize && Math.abs(lat - destinationLat) < stepSize) {

        }
        return result;
    }

    private static double heading(final double originLon, final double originLat, final double destinationLon, final double destinationLat) {
        final double e = 0;
        return (Math.sin(destinationLat) - Math.sin(destinationLon) * Math.cos(e)) / (Math.cos(originLat) * Math.sin(e));
    }

    public static void main(String[] args) {
        greatCirclePolyline(new Point2D(20, 20), new Point2D(-60, 50), 1);
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

    public static double distanceOnMsl(final Point2D p1, final Point2D p2) {
        return distance(p1.getX(), p1.getY(), 0, p2.getX(), p2.getY(), 0);
    }

    public static double squareDistance(final double x1, final double y1, final double x2, final double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    /*
    https://stackoverflow.com/a/16794680/3000387
     */
    private static double distance(
            final double lat1, final double lon1, final double el1,
            final double lat2, final double lon2, final double el2
    ) {

        final int r = 6371; // Radius of the earth

        final double latDistance = Math.toRadians(lat2 - lat1);
        final double lonDistance = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = r * c * 1000; // convert to meters

        final double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
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
