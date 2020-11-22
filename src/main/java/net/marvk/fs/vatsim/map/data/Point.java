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
}
