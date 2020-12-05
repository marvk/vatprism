package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Circle2D;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class SelectionShapePainter extends MapPainter<Object> {
    @Parameter("Fill Color")
    private Color fillColor = Color.SEASHELL.deriveColor(0, 1, 1, 0.05);

    @Parameter("Stroke Color")
    private Color strokeColor = Color.SEASHELL;

    @Parameter(value = "Stroke Width", min = 0, max = 20)
    private double strokeWidth = 1;

    @Parameter("Fill")
    private boolean fill = true;

    @Parameter("Stroke")
    private boolean stroke = true;

    public SelectionShapePainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext context, final Object shape) {
        if (shape instanceof Circle2D) {
            final Circle2D circle = (Circle2D) shape;
            final Point2D c = circle.getCenter();
            final double d = mapVariables.worldWidthToViewWidth(circle.getRadius()) * 2;

            final double rHalf = (d / 2) + 0.5;

            if (fill) {
                context.setFill(fillColor);
                context.fillOval(c.getX() - rHalf, c.getY() - rHalf, d, d);
            }
            if (stroke) {
                context.setLineWidth(strokeWidth);
                context.setStroke(strokeColor);
                context.strokeOval(c.getX() - rHalf, c.getY() - rHalf, d, d);
            }
        }
    }
}
