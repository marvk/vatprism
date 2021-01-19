package net.marvk.fs.vatsim.map.view.painter;

import com.sun.javafx.geom.Line2D;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.time.Duration;

public class DistanceMeasurePainter extends MapPainter<Line2D> {
    private static final double W = 10;
    private static final double W_HALF = W / 2.0;
    @Parameter("Color")
    private Color color = Color.PINK;

    @Parameter("Text Color")
    private Color textColor = Color.PINK;

    @Parameter("Great Circle Line")
    private boolean greatCircle = true;

    @Parameter("Estimated Duration")
    private boolean displayEstimatedDuration = false;

    @Parameter("Estimation Ground Speed In Knots")
    private int estimationKnots = 450;

    private Point2D[] polylineBuffer = new Point2D[51];

    public DistanceMeasurePainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final Line2D line2D) {
        if (line2D == null) {
            return;
        }
        final float x1 = line2D.x1;
        final float y1 = line2D.y1;
        final float x2 = line2D.x2;
        final float y2 = line2D.y2;

        c.setStroke(color);
        painterHelper.strokeOval(c, x1 - W_HALF, y1 - W_HALF, W, W);

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
                "est. %s".formatted(durationString),
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
