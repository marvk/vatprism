package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class BackgroundPainter extends MapPainter<Void> {
    @Parameter("Color")
    private Color color;

    public BackgroundPainter(final MapVariables mapVariables, final Color color) {
        super(mapVariables);
        this.color = color;
    }

    @Override
    public void paint(final GraphicsContext c, final Void unused) {
        c.setFill(color);
        c.fillRect(0, 0, mapVariables.getViewWidth(), mapVariables.getViewHeight());
    }
}
