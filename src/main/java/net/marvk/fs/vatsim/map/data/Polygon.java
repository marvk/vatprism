package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import lombok.ToString;
import net.marvk.fs.vatsim.api.data.Point;
import org.geotools.polylabel.PolyLabeller;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.*;
import java.util.stream.IntStream;

@ToString
public class Polygon {
    private final double[] pointsX;
    private final double[] pointsY;

    private final Rectangle2D boundary;
    private final Point2D polylabel;

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

        this.boundary = new Rectangle2D(minX, minY, maxX - minX, maxY - minY) {
            @Override
            public boolean contains(final double x, final double y) {
                return super.contains(x, y) || super.contains(x - 360, y) || super.contains(x + 360, y);
            }
        };

        this.polylabel = extracted();
    }

    private Point2D extracted() {
        try {
            if (numPoints() <= 2) {
                return null;
            }

            final Coordinate[] objects = IntStream
                    .rangeClosed(0, numPoints())
                    .mapToObj(e -> new Coordinate(pointsX[e % numPoints()], pointsY[e % numPoints()]))
                    .toArray(Coordinate[]::new);

            final org.locationtech.jts.geom.Polygon polygon = new org.locationtech.jts.geom.Polygon(
                    new LinearRing(new CoordinateArraySequence(objects, 2), new GeometryFactory()), new LinearRing[0], new GeometryFactory()
            );
            final org.locationtech.jts.geom.Point polylabel = (org.locationtech.jts.geom.Point) PolyLabeller.getPolylabel(polygon, 1);

            return new Point2D(polylabel.getX(), polylabel.getY());
        } catch (final IllegalStateException | IllegalArgumentException e) {
            return null;
        }
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

        final Map<Integer, Integer> sameMap = new HashMap<>();
        final List<Integer> sameJ = new ArrayList<>();
        final List<Integer> sameI = new ArrayList<>();

        for (int i = 0; i < polygon1.numPoints(); i++) {
            final double p1x = polygon1.pointsX[i];
            final double p1y = polygon1.pointsY[i];

            for (int j = 0; j < polygon2.numPoints(); j++) {
                final double p2x = polygon2.pointsX[j];
                final double p2y = polygon2.pointsY[j];

                if (Double.compare((p1x + 720) % 360, (p2x + 720) % 360) == 0 && Double.compare(p1y, p2y) == 0) {
                    sameMap.put(i, j);
                    sameI.add(i);
                    sameJ.add(j);
                    break;
                }
            }
        }

        final boolean reverse;

        //Some extension polygons have the wrong rotation
        if (sameJ.get(0) > sameJ.get(sameJ.size() - 1)) {
            Collections.reverse(sameJ);
            reverse = true;
        } else {
            reverse = false;
        }

        final List<Point2D> result;
        if (sameJ.get(0) + 1 == sameJ.get(1)) {
            result = mergeWithLineOverlap(polygon1, polygon2, sameMap, sameI, reverse);
        } else {
            result = mergeWithEndPointOverlap(polygon1, polygon2, sameMap);
        }

        return new Polygon(result,
                (e, i) -> e.get(i).getX(),
                (e, i) -> e.get(i).getY(),
                result.size()
        );
    }

    private static List<Point2D> mergeWithEndPointOverlap(final Polygon polygon1, final Polygon polygon2, final Map<Integer, Integer> sameMap) {
        final List<Point2D> result = new ArrayList<>();

        for (int i = 0; i < polygon1.numPoints(); i++) {
            result.add(new Point2D(normalizeX(polygon1.pointsX[i]), polygon1.pointsY[i]));
        }

        final boolean reverse = sameMap.get(0) == 0;

        for (int i = 0; i < polygon2.numPoints(); i++) {
            final int index = reverse ? polygon2.numPoints() - i - 1 : i;
            result.add(new Point2D(normalizeX(polygon2.pointsX[index]), polygon2.pointsY[index]));
        }

        return result;
    }

    private static List<Point2D> mergeWithLineOverlap(
            final Polygon polygon1,
            final Polygon polygon2,
            final Map<Integer, Integer> sameMap,
            final List<Integer> sameI,
            final boolean reverse
    ) {
        final List<Point2D> result = new ArrayList<>();

        final int step = reverse ? 1 : -1;

        for (int i = 0; i < polygon1.numPoints(); i++) {
            result.add(new Point2D(normalizeX(polygon1.pointsX[i]), polygon1.pointsY[i]));

            final Integer sameIndex = sameMap.get(i);

            if (sameIndex != null) {
                for (
                        int j = sameIndex + step;
                        !sameMap.containsValue(j);
                        j = (polygon2.numPoints() + j + step) % polygon2.numPoints()
                ) {
                    result.add(new Point2D(normalizeX(polygon2.pointsX[j]), polygon2.pointsY[j]));
                }

                i = sameI.get(sameI.size() - 1);
            }
        }
        return result;
    }

    private static double normalizeX(final double e) {
        return e < 0 ? e + 360 : e;
    }

    private static void reverse(final double[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            double temp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = temp;
        }
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

    public Point2D getPolylabel() {
        return polylabel;
    }
}
