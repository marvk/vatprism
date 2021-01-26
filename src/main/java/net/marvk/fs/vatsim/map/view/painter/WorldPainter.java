package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class WorldPainter extends MapPainter<Polygon> {
    @Parameter("Fill")
    private boolean fill = true;

    @Parameter("Fill color")
    private Color fillColor;

    @Parameter("Stroke")
    private boolean stroke = false;

    @Parameter("Stroke Color")
    private Color strokeColor;

    @Parameter(value = "Stroke Width", min = 0, max = 10)
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
