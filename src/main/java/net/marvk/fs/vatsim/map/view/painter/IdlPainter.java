package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.InternationalDateLineViewModel;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class IdlPainter extends MapPainter<InternationalDateLineViewModel> {
    private final Color color;

    public IdlPainter(final MapVariables mapVariables, final Color color) {
        super(mapVariables);
        this.color = color;
    }

    @Override
    public void paint(final GraphicsContext c, final InternationalDateLineViewModel internationalDateLineViewModel) {
        final Polygon points = internationalDateLineViewModel.polygon();

        c.setLineWidth(1);
        c.setStroke(color);
        c.setLineDashes(1, 10);

        painterHelper.strokePolylines(c, points);
    }
}
