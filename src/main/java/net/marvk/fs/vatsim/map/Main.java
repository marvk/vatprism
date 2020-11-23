package net.marvk.fs.vatsim.map;

import lombok.SneakyThrows;
import net.marvk.fs.vatsim.api.HttpDataSource;
import net.marvk.fs.vatsim.api.SimpleVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.Point;
import net.marvk.fs.vatsim.api.data.*;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.geometry.DirectPosition3D;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Main {
    private static final int SCALE = 10;
    private static final int LON_RANGE = 180;
    private static final int LAT_RANGE = 90;
    private final VatsimData data;
    private final VatsimFirBoundaries vatsimFirBoundaries;
    private final VatsimApi api;

    @SneakyThrows
    private Main() {
        this.api = new SimpleVatsimApi(new HttpDataSource());
        this.data = api.data();
        this.vatsimFirBoundaries = api.firBoundaries();
    }

    public static void main(final String[] args) {
        new Main().doTheThing();
    }

    @SneakyThrows
    private void doTheThing() {
        final BufferedImage image = new BufferedImage(LON_RANGE * 2 * SCALE + 1, LAT_RANGE * 2 * SCALE + 1, BufferedImage.TYPE_INT_ARGB);
        paintBackground(image, Color.decode("#100B00"));
        paintFirBoundaries(image, Color.decode("#3B341F"));
        paintActiveFirBoundaries(image, Color.decode("#3B341F"));
        paintWorld(image, Color.decode("#A5CBC3"));
        paintVatsimData(image, Color.decode("#85cb33"));

        ImageIO.write(image, "png", new File("out/" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".png"));
    }

    private void paintActiveFirBoundaries(final BufferedImage image, final Color color) throws VatsimApiException {
        final Graphics2D g = (Graphics2D) image.getGraphics();
    }

    private void paintBackground(final BufferedImage image, final Color color) {
        final Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

    private void paintFirBoundaries(final BufferedImage image, final Color color) {
        final Graphics2D g = image.createGraphics();
        for (final VatsimAirspace airspace : vatsimFirBoundaries) {
            if (!"enbd".equalsIgnoreCase(airspace.getGeneral().getIcao())) {
                continue;
            }

            final List<Point> points = airspace.getAirspacePoints()
                                               .stream()
                                               .map(Main::geoToFlat)
                                               .map(p -> new Point(Math.min(p.getX(), LON_RANGE * 2 * SCALE), Math.min(p
                                                       .getY(), LAT_RANGE * 2 * SCALE)))
                                               .collect(Collectors.toList());

            drawPolygon(image, g, color, false, points);
        }
    }

    @SneakyThrows
    private static void paintWorld(final BufferedImage image, final Color color) {
        final ShapefileReader shapefileReader = new ShapefileReader(
                new ShpFiles(Main.class.getResource("ne_110m_coastline/ne_110m_coastline.shp")),
                false,
                false,
                new GeometryFactory()
        );

        final Graphics2D g = (Graphics2D) image.getGraphics();

        while (shapefileReader.hasNext()) {
            final ShapefileReader.Record record = shapefileReader.nextRecord();

            MultiLineString s = (MultiLineString) record.shape();

            final List<Point> points = Arrays.stream(s.getCoordinates())
                                             .map(e -> geoToFlat(e.getX(), e.getY()))
                                             .collect(Collectors.toList());

            points.forEach(p -> image.setRGB(p.getX().intValue(), p.getY().intValue(), Color.BLACK.getRGB()));

            if (points.isEmpty()) {
                continue;
            }

            drawPolygon(image, g, color, true, points);
        }
    }

    private static void drawPolygon(final BufferedImage image, final Graphics2D g, final Color color, final boolean open, final List<Point> points) {
        g.setColor(color);

        Point current = open ? points.get(0) : points.get(points.size() - 1);

        for (int i = open ? 1 : 0; i < points.size(); i++) {
            final Point next = points.get(i);

            image.setRGB(current.getX().intValue(), current.getY().intValue(), color.getRGB());
            if (current.equals(next)) {
                image.setRGB(current.getX().intValue(), current.getY().intValue(), color.getRGB());
            } else {
                g.drawLine(current.getX().intValue(), current.getY().intValue(), next.getX().intValue(), next.getY()
                                                                                                             .intValue());
            }

            current = next;
        }
    }

    //    private static void drawPolygon(final BufferedImage image, final Graphics2D g, final List<Point> points, final boolean open) {
//        final Color color = Color.decode("#3B341F");
//        g.setColor(color);
//
//        final int n = points.size();
//
//        final int limit = Math.max(0, open ? n - 10 : n);
//        final int[] x = points.stream().mapToDouble(Point::getX).mapToInt(e -> (int) e).limit(limit).toArray();
//        final int[] y = points.stream().mapToDouble(Point::getY).mapToInt(e -> (int) e).limit(limit).toArray();
//
//        g.drawPolygon(x, y, x.length);
//    }
//
    @SneakyThrows
    private void paintVatsimData(final BufferedImage image, final Color color) {
        final List<DirectPosition3D> positions =
                data.getClients()
                    .stream()
                    .filter(e -> "PILOT".equals(e.getClientType()))
                    .map(Main::clientToPosition)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        positions.stream()
                 .map(p -> geoToFlat(p.x, p.y))
                 .forEach(point -> {
                     try {
                         image.setRGB(
                                 point.getX().intValue(),
                                 point.getY().intValue(),
                                 color.getRGB()
                         );
                     } catch (Exception e) {
                     }
                 });
    }

    private static Point geoToFlat(final double x, final double y) {
        return new Point((double) Math.round((x + LON_RANGE) * SCALE), (double) Math.round((2 * LAT_RANGE - y - LAT_RANGE) * SCALE));
    }

    private static Point geoToFlat(final Point point) {
        return geoToFlat(point.getX(), point.getY());
    }

    private static DirectPosition3D clientToPosition(final VatsimClient client) {
        final Double longitude = parse(client.getLongitude());
        final Double latitude = parse(client.getLatitude());
        final Double altitude = parse(client.getAltitude());

        if (latitude == null || longitude == null || altitude == null) {
            return null;
        }

        return new DirectPosition3D(longitude, latitude, altitude);
    }

    private static Double parse(final String s) {
        if (s == null || s.isBlank()) {
            return null;
        }

        return Double.parseDouble(s);
    }
}
