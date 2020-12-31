package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class WorldPainter extends MapPainter<Polygon> {
    @Parameter("Color")
    private final Color color;

    public WorldPainter(final MapVariables mapVariables, final Color color) {
        super(mapVariables);
        this.color = color;
    }

    @Override
    public void paint(final GraphicsContext c, final Polygon polygon) {
        c.setFill(color);

        painterHelper.fillPolygons(c, polygon);
    }
}
