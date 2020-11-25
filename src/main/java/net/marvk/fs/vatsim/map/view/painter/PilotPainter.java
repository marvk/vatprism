package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.ClientViewModel;
import net.marvk.fs.vatsim.map.data.RawClientType;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class PilotPainter extends MapPainter<ClientViewModel> {
    private static final Color COLOR = Color.valueOf("85cb33");
    private static final int TAIL_LENGTH = 5;

    public PilotPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final Canvas canvas, final ClientViewModel client) {
        if (client.rawClientTypeProperty().get() != RawClientType.PILOT) {
            return;
        }

        final Point2D point = client.clientStatus().position().get();
        final double xPrecise = mapVariables.toCanvasX(point.getX());
        final int x = (int) xPrecise;
        final double yPrecise = mapVariables.toCanvasY(point.getY());
        final int y = (int) yPrecise;

        final GraphicsContext c = canvas.getGraphicsContext2D();
        c.setStroke(COLOR);
        c.setLineWidth(1);
        c.strokeRect(x - 0.5, y - 0.5, 2, 2);
        final double heading = client.clientStatus().heading().get();

        final double rad = Math.toRadians(heading);
        final int x2 = (int) (xPrecise + Math.sin(rad) * TAIL_LENGTH);
        final int y2 = (int) (yPrecise - Math.cos(rad) * TAIL_LENGTH);

        c.strokeLine(x + 0.5, y + 0.5, x2 + 0.5, y2 + 0.5);
    }
}
