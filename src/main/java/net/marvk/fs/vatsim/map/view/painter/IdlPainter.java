package net.marvk.fs.vatsim.map.view.painter;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import net.marvk.fs.vatsim.map.data.InternationalDateLineViewModel;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class IdlPainter extends MapPainter<InternationalDateLineViewModel> {
    public IdlPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final Canvas canvas, final InternationalDateLineViewModel internationalDateLineViewModel) {
        final ObservableList<Point2D> points = internationalDateLineViewModel.points();

        final GraphicsContext c = canvas.getGraphicsContext2D();

        c.setLineWidth(1);
        c.setLineDashes(1, 10);
        for (int i = 0; i < points.size(); i++) {
            final Point2D point2D = points.get(i);

            mapVariables.getXBuf()[i] = mapVariables.toCanvasX(point2D.getX());
            mapVariables.getYBuf()[i] = mapVariables.toCanvasY(point2D.getY());
        }
        c.strokePolyline(mapVariables.getXBuf(), mapVariables.getYBuf(), points.size());
        c.setLineDashes(null);
    }
}
