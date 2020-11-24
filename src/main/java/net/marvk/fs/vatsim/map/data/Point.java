package net.marvk.fs.vatsim.map.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Point {
    private final Double longitude;
    private final Double latitude;
    private final Double altitude;

    public Point(final Double longitude, final Double latitude) {
        this(longitude, latitude, null);
    }

    public Double getX() {
        return longitude;
    }

    public Double getY() {
        return latitude;
    }

    public Double getZ() {
        return altitude;
    }

    public static Point from(final String longitude, final String latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        return new Point(Double.parseDouble(longitude), Double.parseDouble(latitude));
    }

    public static Point from(final Double longitude, final Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        return new Point(longitude, latitude);
    }

    public double distanceOnMsl(final Point other) {
        return distance(latitude, longitude, 0, other.latitude, other.longitude, 0);
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
}
