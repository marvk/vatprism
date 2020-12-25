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

public class PainterHelper {
    private static final double MIN_DISTANCE = 1.9;
    private static final double MIN_SQUARE_DISTANCE = MIN_DISTANCE * MIN_DISTANCE;
    private final MapVariables mapVariables;

    public PainterHelper(final MapVariables mapVariables) {
        this.mapVariables = mapVariables;
    }

    public void strokePolygons(final GraphicsContext c, final Polygon polygon) {
        drawPolygons(c, polygon, false, false);
    }

    public void strokePolylines(final GraphicsContext c, final Polygon polygon) {
        drawPolygons(c, polygon, true, false);
    }

    public void fillPolygons(final GraphicsContext c, final Polygon polygon) {
        drawPolygons(c, polygon, false, true);
    }

    private void drawPolygons(final GraphicsContext c, final Polygon polygon, final boolean polyline, final boolean fill) {
        if (mapVariables.toCanvasX(polygon.boundary().getMinX()) < 0) {
            drawPolygon(c, polygon, 360, polyline, fill);
        }

        if (mapVariables.toCanvasX(polygon.boundary().getMaxX()) > mapVariables.getViewWidth()) {
            drawPolygon(c, polygon, -360, polyline, fill);
        }

        drawPolygon(c, polygon, 0, polyline, fill);
    }

    private void drawPolygon(final GraphicsContext c, final Polygon polygon, final double offsetX, final boolean polyline, final boolean fill) {
        if (!mapVariables.isIntersectingWorldView(shiftedBounds(polygon, offsetX))) {
            return;
        }

        final int numPoints = writePolygonToBuffer(polygon, offsetX);

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

    private int writePolygonToBuffer(final Polygon polygon, final double offsetX) {
        double lastX = Double.MAX_VALUE;
        double lastY = Double.MAX_VALUE;

        int numPoints = 0;

        int currentPoints = 0;
        double currentXSum = 0;
        double currentYSum = 0;

        for (int i = 0; i < polygon.size(); i++) {
            final double x = mapVariables.toCanvasX(polygon.getPointsX()[i] + offsetX);
            final double y = mapVariables.toCanvasY(polygon.getPointsY()[i]);

            currentPoints += 1;
            currentXSum += x;
            currentYSum += y;

            if (GeomUtil.squareDistance(lastX, lastY, x, y) > MIN_SQUARE_DISTANCE) {
                mapVariables.setXBuf(numPoints, currentXSum / currentPoints);
                mapVariables.setYBuf(numPoints, currentYSum / currentPoints);
                lastX = x;
                lastY = y;

                numPoints += 1;

                currentPoints = 0;
                currentXSum = 0;
                currentYSum = 0;
            }
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
