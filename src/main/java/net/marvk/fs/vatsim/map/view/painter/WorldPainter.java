package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class WorldPainter extends MapPainter<Polygon> {
    private static final Color color = Color.valueOf("A5CBC3");

    public WorldPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final Canvas canvas, final Polygon polygon) {
        final GraphicsContext c = canvas.getGraphicsContext2D();

        c.setLineWidth(1);
        c.setFill(color);

        painterHelper.fillPolygons(c, polygon);
    }
}
