package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Circle2D;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class SelectionShapePainter extends MapPainter<Object> {
    @Parameter(name = "Stroke", group = "Stroke")
    private boolean stroke = true;
    @Parameter(name = "Stroke Color", group = "Stroke")
    private Color strokeColor = Color.SEASHELL;
    @Parameter(name = "Stroke Width", group = "Stroke", min = 0, max = 20)
    private double strokeWidth = 1;

    @Parameter(name = "Fill", group = "Fill")
    private boolean fill = true;
    @Parameter(name = "Fill Color", group = "Fill")
    private Color fillColor = Color.SEASHELL.deriveColor(0, 1, 1, 0.05);

    public SelectionShapePainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final Object shape) {
        if (shape instanceof Circle2D) {
            final Circle2D circle = (Circle2D) shape;
            final Point2D p = circle.getCenter();
            final double d = mapVariables.worldWidthToViewWidth(circle.getRadius()) * 2;

            final double rHalf = (d / 2) + 0.5;

            if (fill) {
                c.setFill(fillColor);
                painterHelper.fillOval(c, p.getX() - rHalf, p.getY() - rHalf, d, d);
            }
            if (stroke) {
                c.setLineDashes((double[]) null);
                c.setLineWidth(strokeWidth);
                c.setStroke(strokeColor);
                painterHelper.strokeOval(c, p.getX() - rHalf, p.getY() - rHalf, d, d);
            }
        }
    }
}
