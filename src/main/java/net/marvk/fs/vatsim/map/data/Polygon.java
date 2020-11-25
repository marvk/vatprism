package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Rectangle2D;
import net.marvk.fs.vatsim.api.data.Point;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

public class Polygon {
    private final double[] pointsX;
    private final double[] pointsY;

    private final Rectangle2D boundary;

    public Polygon(final List<Point> points) {
        this(points, (e, i) -> e.get(i).getX(), (e, i) -> e.get(i).getY(), points.size());
    }

    public Polygon(final Geometry geometry) {
        this(geometry.getCoordinates(), (e, i) -> e[i].getX(), (e, i) -> e[i].getY(), geometry.getCoordinates().length);
    }

    public <T> Polygon(final T t, final CoordinateExtractor<T> xExtractor, final CoordinateExtractor<T> yExtractor, final int length) {
        this.pointsX = new double[length];
        this.pointsY = new double[length];

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;

        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < length; i++) {
            final double x = xExtractor.extract(t, i);
            final double y = yExtractor.extract(t, i);

            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);

            this.pointsX[i] = x;
            this.pointsY[i] = y;
        }

        this.boundary = new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

//    public Polygon(final MultiPolygon m) {
//        this(m, new CoordinateExtractor<MultiPolygon>() {
//            @Override
//            public double extract(final MultiPolygon multiPolygon, final int index) {
//                return 0;
//            }
//        }, new CoordinateExtractor<MultiPolygon>() {
//            @Override
//            public double extract(final MultiPolygon multiPolygon, final int index) {
//                return 0;
//            }
//        }, m.getCoordinates().length);
//    }

    public int size() {
        return pointsX.length;
    }

    public double[] getPointsX() {
        return pointsX;
    }

    public double[] getPointsY() {
        return pointsY;
    }

    public Rectangle2D boundary() {
        return boundary;
    }

    @FunctionalInterface
    public interface CoordinateExtractor<T> {
        double extract(final T t, final int index);
    }
}
