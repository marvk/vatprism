package net.marvk.fs.vatsim.map.view.painter;

import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.List;

public class PainterHelper {
    private static final double MIN_DISTANCE = 2;
    private static final double MIN_SQUARE_DISTANCE = squareThreshold(MIN_DISTANCE);

    private static final double JUMP_THRESHOLD = 128;
    private static final double JUMP_THRESHOLD_SQUARED = squareThreshold(JUMP_THRESHOLD);

    private static double squareThreshold(final double value) {
        return value <= 0 ? Integer.MIN_VALUE : value * value;
    }

    private final MapVariables mapVariables;

    public PainterHelper(final MapVariables mapVariables) {
        this.mapVariables = mapVariables;
    }

    public void strokePolygons(final GraphicsContext c, final Polygon polygon) {
        drawPolygons(c, polygon, false, false, true);
    }

    public void strokePolylines(final GraphicsContext c, final Polygon polygon) {
        drawPolygons(c, polygon, true, false, true);
    }

    public void fillPolygons(final GraphicsContext c, final Polygon polygon) {
        drawPolygons(c, polygon, false, true, true);
    }

    private void drawPolygons(final GraphicsContext c, final Polygon polygon, final boolean polyline, final boolean fill, final boolean simplify) {
        if (mapVariables.toCanvasX(polygon.boundary().getMinX()) < 0) {
            drawPolygon(c, polygon, 360, polyline, fill, simplify);
        }

        if (mapVariables.toCanvasX(polygon.boundary().getMaxX()) > mapVariables.getViewWidth()) {
            drawPolygon(c, polygon, -360, polyline, fill, simplify);
        }

        drawPolygon(c, polygon, 0, polyline, fill, simplify);
    }

    private void drawPolygon(final GraphicsContext c, final Polygon polygon, final double offsetX, final boolean polyline, final boolean fill, final boolean simplify) {
        if (!mapVariables.isIntersectingWorldView(shiftedBounds(polygon, offsetX))) {
            return;
        }

        final int numPoints = writePolygonToBuffer(c, polygon, offsetX);

        final boolean twoDimensional = numPoints >= 3;
        if (!twoDimensional) {
            return;
        }

        if (polyline) {
            if (!fill) {
                c.strokePolyline(mapVariables.getXBuf(), mapVariables.getYBuf(), numPoints);
            }
        } else {
            if (fill) {
                c.fillPolygon(mapVariables.getXBuf(), mapVariables.getYBuf(), numPoints);
            } else {
                c.strokePolygon(mapVariables.getXBuf(), mapVariables.getYBuf(), numPoints);
            }
        }
    }

    private int writePolygonToBuffer(final GraphicsContext c, final Polygon polygon, final double offsetX) {
        int numPoints = writeRingToBuffer(polygon.getExteriorRing(), offsetX, c, 0);

        final List<Polygon.Ring> holeRings = polygon.getHoleRings();

        for (final Polygon.Ring hole : holeRings) {
            numPoints += writeRingToBuffer(hole, offsetX, c, numPoints);
        }

        for (int i = 0; i < holeRings.size() - 1; i++) {
            final Polygon.Ring hole = holeRings.get(holeRings.size() - 2 - i);
            mapVariables.setBuf(numPoints, mapVariables.toCanvasX(hole.getPointsX()[0] + offsetX), mapVariables.toCanvasY(hole
                    .getPointsY()[0]));
            numPoints += 1;
        }

        return numPoints;
    }

    private int writeRingToBuffer(final Polygon.Ring ring, final double offsetX, final GraphicsContext c, final int indexOffset) {
        double lastDrawnX = Double.MAX_VALUE;
        double lastDrawnY = Double.MAX_VALUE;

        double lastX = Double.NaN;
        double lastY = Double.NaN;

        int numPoints = 0;

        int currentPoints = 0;
//        double currentXSum = 0;
//        double currentYSum = 0;

        for (int i = 0; i < ring.numPoints(); i++) {
            final double x = mapVariables.toCanvasX(ring.getPointsX()[i] + offsetX);
            final double y = mapVariables.toCanvasY(ring.getPointsY()[i]);

            currentPoints += 1;
//            currentXSum += x;
//            currentYSum += y;

            final double squareDistance = GeomUtil.squareDistance(lastDrawnX, lastDrawnY, x, y);

            if (i == 0 || i == ring.numPoints() - 1 || squareDistance > MIN_SQUARE_DISTANCE) {
//                if (currentPoints > 1 && squareDistance > JUMP_THRESHOLD_SQUARED) {
//                    mapVariables.setXBuf(numPoints, lastX);
//                    mapVariables.setYBuf(numPoints, lastY);
//
//                    numPoints += 1;
//                }

                mapVariables.setBuf(indexOffset + numPoints, x, y);

                numPoints += 1;

                lastDrawnX = x;
                lastDrawnY = y;

                currentPoints = 0;
//                currentXSum = 0;
//                currentYSum = 0;
            }

            lastX = x;
            lastY = y;
        }

        return numPoints;
    }

    private static Rectangle2D shiftedBounds(final Polygon polygon, final double offsetX) {
        final Rectangle2D boundary = polygon.boundary();
        return new Rectangle2D(
                boundary.getMinX() + offsetX,
                boundary.getMinY(),
                boundary.getWidth(),
                boundary.getHeight()
        );
    }

    public void setPixel(final GraphicsContext c, final Color color, final int x, final int y) {
        final int actualX;

        if (x < 0) {
            actualX = (int) (x + mapVariables.getViewWidth() * mapVariables.getScale());
        } else if (x >= mapVariables.getViewWidth()) {
            actualX = (int) (x - mapVariables.getViewWidth() * mapVariables.getScale());
        } else {
            actualX = x;
        }

        c.getPixelWriter().setColor(
                actualX,
                y,
                color
        );
    }

    public void fillTextWithBackground(final GraphicsContext c, final double x, final double y, final String text, final boolean background, final VPos baseline, final Color textColor, final Color backgroundColor) {
        if (background) {
            c.setTextBaseline(baseline);
            final FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(c.getFont());

            c.setFill(backgroundColor);
            final int width = (int) Math.round(text.chars().mapToDouble(e -> fm.getCharWidth((char) e)).sum());
            final int height = Math.round(fm.getLineHeight());

            final double baselineOffset = switch (baseline) {
                case BOTTOM -> 0;
                case CENTER -> height / 2.0;
                default -> throw new IllegalArgumentException("Illegal baseline " + baseline);
            };

            final double xRect = Math.floor(x - width / 2.0);
            final double yRect = Math.ceil(y - 1 - baselineOffset) + 1;
            c.fillRect(xRect, yRect, width + 1, height);
        }

        fillText(c, x, y, text, textColor, baseline);
    }

    public void fillText(final GraphicsContext c, final double x, final double y, final String text, final Color color, final VPos baseline) {
        c.setFill(color);
        c.setTextBaseline(baseline);
        c.fillText(text, x, y);
    }
}
