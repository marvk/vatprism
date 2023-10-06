package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.DistanceMeasure;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.time.Duration;

public class DistanceMeasurePainter extends MapPainter<DistanceMeasure> {
    private static final double W = 10;

    @Parameter(name = "Color")
    private Color color = Color.web("#a05260");

    @Parameter(name = "Text Color")
    private Color textColor = Color.web("#481720");

    @Parameter(name = "Great Circle Line", hintText = "When enabled, shows tracks as great circle lines. When disabled, shows tracks as straight lines on the map.")
    private boolean greatCircle = true;

    @Parameter(name = "Persistent", hintText = "If disabled, measure will disappear as soon as the map is clicked")
    protected boolean persistent = true;

    @Parameter(name = "Estimated Duration")
    private boolean displayEstimatedDuration = false;

    @Parameter(name = "Estimation Ground Speed In Knots", min = 1)
    private int estimationKnots = 450;

    private Point2D[] polylineBuffer = new Point2D[51];

    public DistanceMeasurePainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final DistanceMeasure distanceMeasure) {
        if (distanceMeasure == null || (!persistent && distanceMeasure.isReleased())) {
            return;
        }
        final double x1 = mapVariables.toCanvasX(distanceMeasure.getFrom().getX());
        final double y1 = mapVariables.toCanvasY(distanceMeasure.getFrom().getY());
        final double x2 = mapVariables.toCanvasX(distanceMeasure.getTo().getX());
        final double y2 = mapVariables.toCanvasY(distanceMeasure.getTo().getY());

        c.setLineDashes(null);
        c.setLineWidth(1);
        c.setStroke(color);
        c.setFill(color);
        paintCircles(c, distanceMeasure.getFrom(), W, false);
        if (distanceMeasure.isReleased()) {
            paintCircles(c, distanceMeasure.getTo(), W, false);
        } else {
            paintCircles(c, distanceMeasure.getTo(), 3, true);
        }

        final Point2D from = mapVariables.toWorldBounded(new Point2D(x1, y1));
        final Point2D to = mapVariables.toWorldBounded(new Point2D(x2, y2));

        final double distanceInMeters = GeomUtil.distanceOnMsl(from, to);
        final double distanceInNauticalMiles = GeomUtil.metersToNauticalMiles(distanceInMeters);

        if (greatCircle) {
            final Point2D[] polyline = GeomUtil.greatCirclePolyline(from, to, polylineBuffer);
            painterHelper.strokePolyline(c, polyline);
            final Point2D labelPosition = polyline[25];

            paintText(c, distanceInMeters, distanceInNauticalMiles, labelPosition);
        } else {
            final int offset;

            if (from.getX() - 180 > to.getX()) {
                offset = 360;
            } else if (from.getX() + 180 < to.getX()) {
                offset = -360;
            } else {
                offset = 0;
            }

            painterHelper.strokeLine(c, mapVariables.toCanvasX(from.getX()), y1, mapVariables.toCanvasX(to.getX() + offset), y2);
            painterHelper.strokeLine(c, mapVariables.toCanvasX(from.getX() + 360), y1, mapVariables.toCanvasX(to.getX() + offset + 360), y2);
            painterHelper.strokeLine(c, mapVariables.toCanvasX(from.getX() - 360), y1, mapVariables.toCanvasX(to.getX() + offset - 360), y2);
            paintText(c, distanceInMeters, distanceInNauticalMiles, from.midpoint(to.add(offset, 0)));
        }
    }

    private void paintCircles(final GraphicsContext c, final Point2D world, final double size, final boolean fill) {
        paintCircle(c, world.getX(), world.getY(), size, 0, fill);
        paintCircle(c, world.getX(), world.getY(), size, 360, fill);
        paintCircle(c, world.getX(), world.getY(), size, -360, fill);
    }

    private void paintCircle(final GraphicsContext c, final double x1, final double y1, final double size, final double offset, final boolean fill) {
        if (fill) {
            painterHelper.fillOval(c, mapVariables.toCanvasX(x1 + offset) - (size / 2.0), mapVariables.toCanvasY(y1) - (size / 2.0), size, size);
        } else {
            painterHelper.strokeOval(c, mapVariables.toCanvasX(x1 + offset) - (size / 2.0), mapVariables.toCanvasY(y1) - (size / 2.0), size, size);
        }
    }

    private void paintText(final GraphicsContext c, final double distanceInMeters, final double distanceInNauticalMiles, final Point2D labelPosition) {
        distanceText(c, distanceInNauticalMiles, labelPosition, 0);
        distanceText(c, distanceInNauticalMiles, labelPosition, +360);
        distanceText(c, distanceInNauticalMiles, labelPosition, -360);

        if (displayEstimatedDuration) {
            final double groundSpeedInMetersPerSecond = GeomUtil.knotsToMs(estimationKnots);

            final double seconds = distanceInMeters / groundSpeedInMetersPerSecond;

            final Duration duration = Duration.ofSeconds(Math.round(seconds));

            final int hours = duration.toHoursPart();
            final int minutes = duration.toMinutesPart();

            final String durationString = "%dh%dm".formatted(hours, minutes);

            estimatedTimeText(c, labelPosition, durationString, 0);
            estimatedTimeText(c, labelPosition, durationString, +360);
            estimatedTimeText(c, labelPosition, durationString, -360);
        }
    }

    private void estimatedTimeText(final GraphicsContext c, final Point2D labelPosition, final String durationString, final double offset) {
        painterHelper.fillTextWithBackground(c,
                mapVariables.toCanvasX(labelPosition.getX() + offset),
                mapVariables.toCanvasY(labelPosition.getY()),
                "%s at %skts".formatted(durationString, estimationKnots),
                true,
                TextAlignment.CENTER,
                VPos.TOP,
                textColor,
                color
        );
    }

    private void distanceText(final GraphicsContext c, final double distanceInNauticalMiles, final Point2D labelPosition, final double offset) {
        final VPos distanceVPos = displayEstimatedDuration ? VPos.BOTTOM : VPos.CENTER;

        painterHelper.fillTextWithBackground(c,
                mapVariables.toCanvasX(labelPosition.getX() + offset),
                mapVariables.toCanvasY(labelPosition.getY()),
                "%sNM".formatted((int) Math.round(distanceInNauticalMiles)),
                true,
                TextAlignment.CENTER,
                distanceVPos,
                textColor,
                color
        );
    }
}
