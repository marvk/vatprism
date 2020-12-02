package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class PilotPainter extends MapPainter<Pilot> {
    private static final Color COLOR = Color.valueOf("3b3526").deriveColor(0, 1, 3, 0.25);
    private static final int TAIL_LENGTH = 8;
    private static final int MULTIDRAW_BOUND = 10;
    private static final int TEXT_OFFSET = 10;

    public PilotPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final Pilot client) {
        final Point2D position = client.getPosition();

        final double centerX = mapVariables.toCanvasX(position.getX());

        if (centerX - MULTIDRAW_BOUND < 0) {
            draw(c, client, 360);
        }

        if (centerX + MULTIDRAW_BOUND > mapVariables.getViewWidth()) {
            draw(c, client, -360);
        }

        draw(c, client, 0);
    }

    private void draw(final GraphicsContext c, final Pilot pilot, final int xOffset) {
        final Point2D point = pilot.getPosition();

        final double xPrecise = mapVariables.toCanvasX(point.getX() + xOffset);
        final int x = (int) xPrecise;
        final double yPrecise = mapVariables.toCanvasY(point.getY());
        final int y = (int) yPrecise;

//        c.setLineDashes(null);
        c.setStroke(COLOR);
        c.setFill(COLOR);
        c.setLineWidth(1);
        c.strokeRect(x - 1.5, y - 1.5, 4, 4);
        final double heading = pilot.getHeading();

        final double rad = Math.toRadians(heading);
        final int x2 = (int) (xPrecise + Math.sin(rad) * TAIL_LENGTH);
        final int y2 = (int) (yPrecise - Math.cos(rad) * TAIL_LENGTH);

        c.setTextBaseline(VPos.CENTER);
        c.setTextAlign(TextAlignment.CENTER);

//        c.setLineDashes(1, 5);
        c.strokeLine(x + 0.5, y + 0.5, x2 + 0.5, y2 + 0.5);

        final int yOffset;

        if (heading >= 90 && heading <= 270) {
            yOffset = -TEXT_OFFSET;
        } else {
            yOffset = TEXT_OFFSET;
        }

        c.fillText(pilot.getCallsign(), x, y + yOffset);
    }
}
