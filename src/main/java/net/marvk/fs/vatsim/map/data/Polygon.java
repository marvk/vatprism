package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.Point;
import org.locationtech.jts.geom.MultiLineString;

import java.util.List;

public class Polygon {
    private final double[] pointsX;
    private final double[] pointsY;

    private final Point2D min;
    private final Point2D max;

    public Polygon(final List<Point> points) {
        this(points, (e, i) -> e.get(i).getX(), (e, i) -> e.get(i).getY(), points.size());
    }

    public Polygon(final MultiLineString mls) {
        this(mls.getCoordinates(), (e, i) -> e[i].getX(), (e, i) -> e[i].getY(), mls.getCoordinates().length);
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

        this.min = new Point2D(minX, minY);
        this.max = new Point2D(maxX, maxY);
    }

    public int size() {
        return pointsX.length;
    }

    public double[] getPointsX() {
        return pointsX;
    }

    public double[] getPointsY() {
        return pointsY;
    }

    public Point2D getMin() {
        return min;
    }

    public Point2D getMax() {
        return max;
    }

    @FunctionalInterface
    public interface CoordinateExtractor<T> {
        double extract(final T t, final int index);
    }
}
