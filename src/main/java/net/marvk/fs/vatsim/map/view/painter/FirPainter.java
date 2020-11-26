package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class FirPainter extends MapPainter<FlightInformationRegionBoundaryViewModel> {

    private final Color color;
    private final double lineWidth;
    private final boolean fill;

    public FirPainter(final MapVariables mapVariables, final Color color, final double lineWidth, final boolean fill) {
        super(mapVariables);
        this.color = color;
        this.lineWidth = lineWidth;
        this.fill = fill;
    }

    public FirPainter(final MapVariables mapVariables, final Color color, final double lineWidth) {
        this(mapVariables, color, lineWidth, false);
    }

    @Override
    public void paint(final Canvas canvas, final FlightInformationRegionBoundaryViewModel fir) {
        final GraphicsContext c = canvas.getGraphicsContext2D();

        if (fir.extensionProperty().get()) {
            return;
        }

        final Polygon polygon = fir.getPolygon();

        if (fill) {
            c.setFill(color);
            painterHelper.fillPolygons(c, polygon);
        } else {
            c.setStroke(color);
            c.setLineWidth(lineWidth);
            c.setLineDashes(null);
            painterHelper.strokePolygons(c, polygon);
        }
    }
}
