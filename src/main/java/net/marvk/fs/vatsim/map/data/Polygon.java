package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Rectangle2D;
import lombok.ToString;
import net.marvk.fs.vatsim.api.data.Point;
import org.locationtech.jts.geom.Geometry;

import java.util.Arrays;
import java.util.List;

@ToString
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

    public int numPoints() {
        return pointsX.length;
    }

    public static Polygon merge(final Polygon polygon1, final Polygon polygon2) {
        if (polygon1 == null || polygon1.numPoints() <= 2) {
            return polygon2;
        }

        if (polygon2 == null || polygon2.numPoints() <= 2) {
            return polygon1;
        }

        int sameCount = 0;

        int inARow = 0;

        for (int i = 0; i < polygon1.numPoints(); i++) {
            final double p1x = polygon1.pointsX[i];
            final double p1y = polygon1.pointsY[i];

            int lastSameCount = sameCount;

            for (int j = 0; j < polygon2.numPoints(); j++) {
                final double p2x = polygon2.pointsX[polygon2.numPoints() - 1 - j];
                final double p2y = polygon2.pointsY[polygon2.numPoints() - 1 - j];

                if (Double.compare(Math.abs(p1x), Math.abs(p2x)) == 0 && Double.compare(p1y, p2y) == 0) {
                    sameCount++;
                    break;
                }
            }

            if (lastSameCount == sameCount) {
                if (inARow > 0) {
                    System.out.println("inARow = " + inARow);
                    inARow = 0;
                }
            } else {
                inARow++;
            }
        }

        if (inARow > 0) {
            System.out.println("inARow = " + inARow);
        }

        System.out.println("sameCount = " + sameCount);
        System.out.println("polygon1.numPoints() = " + polygon1.numPoints());
        System.out.println("polygon2.numPoints() = " + polygon2.numPoints());
        System.out.println();

        return polygon1;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Polygon polygon = (Polygon) o;

        if (!Arrays.equals(pointsX, polygon.pointsX)) return false;
        if (!Arrays.equals(pointsY, polygon.pointsY)) return false;
        return boundary != null ? boundary.equals(polygon.boundary) : polygon.boundary == null;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(pointsX);
        result = 31 * result + Arrays.hashCode(pointsY);
        result = 31 * result + (boundary != null ? boundary.hashCode() : 0);
        return result;
    }

    @FunctionalInterface
    public interface CoordinateExtractor<T> {
        double extract(final T t, final int index);
    }
}
