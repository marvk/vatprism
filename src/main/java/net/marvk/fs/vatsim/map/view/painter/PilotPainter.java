package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class PilotPainter extends MapPainter<Pilot> {
    private static final Color COLOR = Color.valueOf("85cb33");
    private static final int TAIL_LENGTH = 5;
    private static final int MULTIDRAW_BOUND = 10;

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

    private void draw(final GraphicsContext c, final Pilot client, final int xOffset) {
        final Point2D point = client.getPosition();

        final double xPrecise = mapVariables.toCanvasX(point.getX() + xOffset);
        final int x = (int) xPrecise;
        final double yPrecise = mapVariables.toCanvasY(point.getY());
        final int y = (int) yPrecise;

        c.setStroke(COLOR);
        c.setLineWidth(1);
        c.strokeRect(x - 0.5, y - 0.5, 2, 2);
        final double heading = client.getHeading();

        final double rad = Math.toRadians(heading);
        final int x2 = (int) (xPrecise + Math.sin(rad) * TAIL_LENGTH);
        final int y2 = (int) (yPrecise - Math.cos(rad) * TAIL_LENGTH);

        c.strokeLine(x + 0.5, y + 0.5, x2 + 0.5, y2 + 0.5);
    }
}
