package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Random;

public class WorldPainter extends MapPainter<Polygon> {
    private final Color color;

    public WorldPainter(final MapVariables mapVariables, final Color color) {
        super(mapVariables);
        this.color = color;
    }

    @Override
    public void paint(final Canvas canvas, final Polygon polygon) {
        final GraphicsContext c = canvas.getGraphicsContext2D();

        final Random current = new Random(polygon.hashCode());

        c.setLineWidth(1);
        c.setFill(color);

        painterHelper.fillPolygons(c, polygon);
    }
}
