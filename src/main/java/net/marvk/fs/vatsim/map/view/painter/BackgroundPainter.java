package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class BackgroundPainter extends MapPainter<Void> {
    public BackgroundPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final Canvas canvas, final Void unused) {
        final GraphicsContext c = canvas.getGraphicsContext2D();
        c.setFill(Color.valueOf("100B00"));
        c.fillRect(0, 0, mapVariables.getViewWidth(), mapVariables.getViewHeight());
    }
}
