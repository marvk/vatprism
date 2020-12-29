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
        this.exteriorRing = new Ring(elements.get(0), xExtractor, yExtractor, lengthSupplier);

        this.holeRings = elements
                .stream()
                .skip(1)
                .map(e -> new Ring(e, xExtractor, yExtractor, lengthSupplier))
                .collect(Collectors.toUnmodifiableList());

        this.numPoints = exteriorRing.numPoints() + holeRings.stream().mapToInt(Ring::numPoints).sum();

        this.name = name;
    }

    private String name() {
        return name == null ? "unnamed polygon" : name + " polygon";
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

        final List<Point2D> result;
        if (sameJ.get(0) + 1 == sameJ.get(1)) {
            result = mergeWithLineOverlap(p1Ring, p2Ring, sameMap, sameI, reverse);
        } else {
            result = mergeWithEndPointOverlap(p1Ring, p2Ring, sameMap);
        }

        return new Polygon(List.of(result),
                (e, i) -> e.get(i).getX(),
                (e, i) -> e.get(i).getY(),
                List::size,
                "%s_%s_merged".formatted(polygon1.name(), polygon2.name())
        );
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
        final boolean isNot2D = p.pointsX[1] == p.pointsX[last] && p.pointsY[1] == p.pointsY[last];

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
                    if (pointsX[i - 1] == x && pointsY[i - 1] == y) {
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
