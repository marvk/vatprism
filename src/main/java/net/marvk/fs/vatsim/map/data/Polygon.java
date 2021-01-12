package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.data.Point;
import org.geotools.polylabel.PolyLabeller;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
@Log4j2
public class Polygon {
    private static final LinearRing[] NO_HOLES = new LinearRing[0];

    private final Ring exteriorRing;
    private final List<Ring> holeRings;

    private final int numPoints;

    private final String name;

    public Polygon(final List<Point> points) {
        this(points, null);
    }

    public Polygon(final List<Point> points, final String name) {
        this(List.of(points), (e, i) -> e.get(i).getX(), (e, i) -> e.get(i).getY(), List::size, name);
    }

    public Polygon(final Geometry geometry) {
        this(geometry, null);
    }

    public Polygon(final Geometry geometry, final String name) {
        this(coordinates(geometry),
                (e, i) -> e.getCoordinates()[i].getX(),
                (e, i) -> e.getCoordinates()[i].getY(),
                e -> e.getCoordinates().length,
                name
        );
    }

    public <E> Polygon(final List<E> elements, final CoordinateExtractor<E> xExtractor, final CoordinateExtractor<E> yExtractor, final ToIntFunction<E> lengthSupplier) {
        this(elements, xExtractor, yExtractor, lengthSupplier, null);
    }

    public <E> Polygon(final List<E> elements, final CoordinateExtractor<E> xExtractor, final CoordinateExtractor<E> yExtractor, final ToIntFunction<E> lengthSupplier, final String name) {
        this.name = name;

        this.exteriorRing = new Ring(elements.get(0), xExtractor, yExtractor, lengthSupplier);

        this.holeRings = elements
                .stream()
                .skip(1)
                .map(e -> new Ring(e, xExtractor, yExtractor, lengthSupplier))
                .collect(Collectors.toUnmodifiableList());

        this.numPoints = exteriorRing.numPoints() + holeRings.stream().mapToInt(Ring::numPoints).sum();
    }

    private String name() {
        return name == null ? "unnamed_polygon" : name + "_polygon";
    }

    public boolean isInside(final Point2D point) {
        return isInside(point.getX(), point.getY());
    }

    public boolean isInside(final double x, final double y) {
        if (hasHoleRings()) {
            throw new UnsupportedOperationException();
        }

        return windingNumber(x, y) != 0;
    }

    public double distance(final Point2D point) {
        return distance(point.getX(), point.getY());
    }

    public double distance(final double x, final double y) {
        if (hasHoleRings()) {
            throw new UnsupportedOperationException();
        }

        if (isInside(x, y)) {
            return 0;
        }

        double minSquareDist = Double.MAX_VALUE;

        for (int i = 0; i < exteriorRing.numPoints(); i++) {
            final double a = squareDist(i, x, y);
            minSquareDist = Math.min(a, minSquareDist);
        }

        return Math.sqrt(minSquareDist);
    }

    private double squareDist(final int lineIndex, final double x, final double y) {
        final int nextLineIndex = (lineIndex + 1) % exteriorRing.numPoints();
        final double x0 = exteriorRing.pointsX[lineIndex];
        final double y0 = exteriorRing.pointsY[lineIndex];
        final double x1 = exteriorRing.pointsX[nextLineIndex];
        final double y1 = exteriorRing.pointsY[nextLineIndex];

        final double dotProduct = dotProduct(x0, y0, x1, y1);
        final double squareMagnitude = squareMagnitude(x0, y0, x1, y1);

        final double t = dotProduct / squareMagnitude;

        if (t <= 0) {
            return squareMagnitude(x0, y0, x, y);
        } else if (t > 1) {
            return squareMagnitude(x1, y1, x, y);
        } else {
            return squareMagnitude(x0 + t * (x1 - x0), y0 + t * (y1 - y0), x, y);
        }
    }

    private static double dotProduct(final double x0, final double y0, final double x1, final double y1) {
        return x0 * x1 + y0 * y1;
    }

    private static double squareMagnitude(final double x0, final double y0, final double x1, final double y1) {
        return (x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1);
    }

    /**
     * Test if a point is {@code Left}, {@code On} or {@code Right} of an infinite line.
     *
     * @param x0 line point 0 x
     * @param y0 line point 0 y
     * @param x1 line point 1 x
     * @param y1 line point 1 y
     * @param xt test point x
     * @param yt test point y
     *
     * @return {@code >0} for P2 left of the line through P0 and P1 <br>
     * {@code =0} for P2  on the line <br>
     * {@code <0} for P2  right of the line
     *
     * @see <a href="http://geomalgorithms.com/a03-_inclusion.html">Point in Polygon Inclusion</a>
     */
    private static double isLeft(final double x0, final double y0, final double x1, final double y1, final double xt, final double yt) {
        return ((x1 - x0) * (yt - y0)
                - (xt - x0) * (y1 - y0));
    }

    /**
     * winding number test for a point in a polygon
     *
     * @param x test point x
     * @param y test point y
     *
     * @return the winding number ({@code =0} only when P is outside)
     *
     * @see <a href="http://geomalgorithms.com/a03-_inclusion.html">Point in Polygon Inclusion</a>
     */
    private int windingNumber(final double x, final double y) {
        int windingNumber = 0;

        final Ring r = this.exteriorRing;
        final int n = r.numPoints();

        for (int i = 0; i < n; i++) {                                                                       // edge from V[i] to  V[i+1]
            final int i1 = (i + 1) % n;
            if (r.pointsY[i] <= y) {                                                                        // start y <= P.y
                if (r.pointsY[i1] > y) {                                                                 // an upward crossing
                    if (isLeft(r.pointsX[i], r.pointsY[i], r.pointsX[i1], r.pointsY[i1], x, y) > 0) { // P left of  edge
                        windingNumber += 1;                                                                 // have  a valid up intersect
                    }
                }
            } else {                                                                                        // start y > P.y (no test needed)
                if (i1 < n && r.pointsY[i1] <= y) {                                                   // a downward crossing
                    if (isLeft(r.pointsX[i], r.pointsY[i], r.pointsX[i1], r.pointsY[i1], x, y) < 0) { // P right of  edge
                        windingNumber -= 1;                                                                 // have  a valid down intersect
                    }
                }
            }
        }

        return windingNumber;
    }

    private static List<Geometry> coordinates(final Geometry geometry) {
        if (geometry instanceof org.locationtech.jts.geom.Polygon) {
            final org.locationtech.jts.geom.Polygon polygon = (org.locationtech.jts.geom.Polygon) geometry;
            final int numInteriorRing = polygon.getNumInteriorRing();

            if (numInteriorRing > 0) {
                final ArrayList<Geometry> result = new ArrayList<>(1 + numInteriorRing);
                result.add(polygon.getExteriorRing());
                for (int i = 0; i < numInteriorRing; i++) {
                    result.add(polygon.getInteriorRingN(i));
                }
                return result;
            }
        }

        return Collections.singletonList(geometry);
    }

    public int size() {
        return numPoints;
    }

    public int numPoints() {
        return numPoints;
    }

    public Rectangle2D boundary() {
        return exteriorRing.boundary;
    }

    public Ring getExteriorRing() {
        return exteriorRing;
    }

    public List<Ring> getHoleRings() {
        return holeRings;
    }

    public boolean hasHoleRings() {
        return !holeRings.isEmpty();
    }

    public static Polygon merge(final Polygon polygon1, final Polygon polygon2) {
        Objects.requireNonNull(polygon1);
        Objects.requireNonNull(polygon2);

        if (isInvalid(polygon1.exteriorRing)) {
            return polygon2;
        }

        if (isInvalid(polygon2.exteriorRing)) {
            return polygon1;
        }

        if (polygon1.hasHoleRings() || polygon2.hasHoleRings()) {
            throw new IllegalArgumentException();
        }

        final Map<Integer, Integer> sameMap = new HashMap<>();
        final List<Integer> sameJ = new ArrayList<>();
        final List<Integer> sameI = new ArrayList<>();

        final Ring p1Ring = polygon1.exteriorRing;
        final Ring p2Ring = polygon2.exteriorRing;

        for (int i = 0; i < p1Ring.numPoints(); i++) {
            final double p1x = p1Ring.pointsX[i];
            final double p1y = p1Ring.pointsY[i];

            for (int j = 0; j < p2Ring.numPoints(); j++) {
                final double p2x = p2Ring.pointsX[j];
                final double p2y = p2Ring.pointsY[j];

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

        final List<Point2D> merged;
        if (sameJ.get(0) + 1 == sameJ.get(1)) {
            merged = mergeWithLineOverlap(p1Ring, p2Ring, sameMap, sameI, reverse);
        } else {
            merged = mergeWithEndPointOverlap(p1Ring, p2Ring, sameMap);
        }

        final String mergedName = "%s_%s_merged".formatted(polygon1.name(), polygon2.name());

        final List<Point2D> result = normalize(mergedName, merged);

        return new Polygon(List.of(result),
                (e, i) -> e.get(i).getX(),
                (e, i) -> e.get(i).getY(),
                List::size,
                mergedName
        );
    }

    private static List<Point2D> normalize(final String name, final List<Point2D> merged) {
        if (merged.get(0).equals(merged.get(merged.size() - 1))) {
            log.debug("Merged polygon %s has same start and end point, removing end point".formatted(name));
            merged.remove(merged.size() - 1);
        }

        if (merged.stream().mapToDouble(Point2D::getX).allMatch(e -> e > 180)) {
            log.debug("Merged polygon %s lies beyond 180 degrees, shifting west by 360 degrees".formatted(name));
            return merged.stream().map(e -> e.add(-360, 0)).collect(Collectors.toList());
        }

        if (merged.stream().mapToDouble(Point2D::getX).allMatch(e -> e < 180)) {
            log.debug("Merged polygon %s lies beyond -180 degrees, shifting east by 360 degrees".formatted(name));
            return merged.stream().map(e -> e.add(360, 0)).collect(Collectors.toList());
        }

        return merged;
    }

    private static boolean isInvalid(final Ring p) {
        final boolean isNotPolygon = p.numPoints() <= 2;
        if (isNotPolygon) {
            log.info("Failed to merge %s, insufficient points for merging (%d)".formatted(
                    p.polygon().name(),
                    p.numPoints()
            ));
            return true;
        }

        final int last = p.numPoints() - 1;
        final boolean isNot2D = Double.compare(p.pointsX[1], p.pointsX[last]) == 0 && Double.compare(p.pointsY[1], p.pointsY[last]) == 0;

        if (isNot2D) {
            log.info("Failed to merge %s, first and last edge match".formatted(p.polygon().name()));
        }

        return isNot2D;
    }

    private static List<Point2D> mergeWithEndPointOverlap(final Ring polygon1, final Ring polygon2, final Map<Integer, Integer> sameMap) {
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
            final Ring polygon1,
            final Ring polygon2,
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
            final double temp = arr[i];
            arr[i] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = temp;
        }
    }

    @FunctionalInterface
    public interface CoordinateExtractor<T> {
        double extract(final T t, final int index);
    }

    public class Ring {
        private final double[] pointsX;
        private final double[] pointsY;

        private final Rectangle2D boundary;

        private Point2D polyLabel = null;

        public <T> Ring(final T t, final CoordinateExtractor<T> xExtractor, final CoordinateExtractor<T> yExtractor, final ToIntFunction<T> lengthSupplier) {
            final int n = lengthSupplier.applyAsInt(t);

            final double[] pointsX = new double[n];
            final double[] pointsY = new double[n];

            double minX = Double.POSITIVE_INFINITY;
            double minY = Double.POSITIVE_INFINITY;

            double maxX = Double.NEGATIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;

            int duplicates = 0;

            for (int i = 0; i < n; i++) {
                final double x = xExtractor.extract(t, i);
                final double y = yExtractor.extract(t, i);

                if (i > 0) {
                    if (Double.compare(pointsX[i - 1], x) == 0 && Double.compare(pointsY[i - 1], y) == 0) {
                        duplicates++;
                        continue;
                    }
                }

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);

                pointsX[i - duplicates] = x;
                pointsY[i - duplicates] = y;
            }

            if (duplicates > 0) {
                log.trace("Removed %d duplicate %s in %s".formatted(duplicates, (duplicates > 1 ? "vertices" : "vertex"), name()));
                this.pointsX = Arrays.copyOf(pointsX, n - duplicates);
                this.pointsY = Arrays.copyOf(pointsY, n - duplicates);
            } else {
                this.pointsX = pointsX;
                this.pointsY = pointsY;
            }

            this.boundary = new Rectangle2D(minX, minY, maxX - minX, maxY - minY) {
                @Override
                public boolean contains(final double x, final double y) {
                    return super.contains(x, y) || super.contains(x - 360, y) || super.contains(x + 360, y);
                }
            };
        }

        private Polygon polygon() {
            return Polygon.this;
        }

        public Point2D getPolyLabel() {
            if (polyLabel == null) {
                if (numPoints() == 1) {
                    polyLabel = new Point2D(pointsX[0], pointsY[0]);
                } else if (numPoints() == 2) {
                    polyLabel = new Point2D(pointsX[0], pointsY[0]).add(pointsX[1], pointsY[1]).multiply(0.5);
                } else {
                    polyLabel = polyLabel();
                }

                if (polyLabel != null) {
                    final double x = (polyLabel.getX() + 180) % 360 - 180;
                    final double y = polyLabel.getY();
                    polyLabel = new Point2D(x, y);
                }
            }

            return polyLabel;
        }

        public int numPoints() {
            return pointsX.length;
        }

        private Point2D polyLabel() {
            final Coordinate[] coordinates = IntStream
                    .rangeClosed(0, numPoints())
                    .mapToObj(e -> new Coordinate(pointsX[e % numPoints()], pointsY[e % numPoints()]))
                    .toArray(Coordinate[]::new);

            try {
                if (numPoints() <= 2) {
                    return null;
                }

                final org.locationtech.jts.geom.Polygon polygon = new org.locationtech.jts.geom.Polygon(
                        new LinearRing(new CoordinateArraySequence(coordinates, 2), new GeometryFactory()), NO_HOLES, new GeometryFactory()
                );

                final org.locationtech.jts.geom.Point polyLabel = (org.locationtech.jts.geom.Point) PolyLabeller.getPolylabel(polygon, 1);

                return new Point2D(polyLabel.getX(), polyLabel.getY());
            } catch (final IllegalStateException | IllegalArgumentException e) {
                log.warn("Failed polylabel for %s: %s".formatted(name(), e.getMessage()));

                return new Point2D(
                        Arrays.stream(pointsX).average().getAsDouble(),
                        Arrays.stream(pointsY).average().getAsDouble()
                );
            }
        }

        public double[] getPointsX() {
            return pointsX;
        }

        public double[] getPointsY() {
            return pointsY;
        }

        public Rectangle2D getBoundary() {
            return boundary;
        }
    }
}
