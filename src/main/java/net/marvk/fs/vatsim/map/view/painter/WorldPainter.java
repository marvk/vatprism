package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class WorldPainter extends MapPainter<Polygon> {
    @Parameter(name = "Fill", group = "Fill")
    private boolean fill = true;

    @Parameter(name = "Fill color", group = "Fill")
    private Color fillColor;

    @Parameter(name = "Stroke", group = "Stroke", disabled = true)
    private boolean stroke = false;

    @Parameter(name = "Stroke Color", group = "Stroke", disabled = true)
    private Color strokeColor;

    @Parameter(name = "Stroke Width", group = "Stroke", min = 0, max = 10, disabled = true)
    private double strokeWidth = 1;

    public WorldPainter(final MapVariables mapVariables, final Color color) {
        super(mapVariables);
        this.fillColor = color;
        this.strokeColor = color;
    }

    @Override
    public void paint(final GraphicsContext c, final Polygon polygon) {
        if (fill) {
            c.setFill(fillColor);
            painterHelper.fillPolygons(c, polygon);
        }

        if (stroke) {
            c.setStroke(strokeColor);
            c.setLineWidth(strokeWidth);
            painterHelper.strokePolygons(c, polygon);
        }

//        c.setLineWidth(1);
//        c.setStroke(Color.BLACK);
//        painterHelper.strokePolygons(c, polygon);
    }
}
