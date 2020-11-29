package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class FirPainter extends MapPainter<FlightInformationRegionBoundaryViewModel> {

    private final Color color;
    private final double lineWidth;
    private final boolean fill;
    private final boolean text;

    public FirPainter(final MapVariables mapVariables, final Color color, final double lineWidth, final boolean fill, final boolean text) {
        super(mapVariables);
        this.color = color;
        this.lineWidth = lineWidth;
        this.fill = fill;
        this.text = text;
    }

    public FirPainter(final MapVariables mapVariables, final Color color, final double lineWidth) {
        this(mapVariables, color, lineWidth, false, false);
    }

    @Override
    public void paint(final GraphicsContext c, final FlightInformationRegionBoundaryViewModel fir) {
        if (fir.extensionProperty().get()) {
            return;
        }

        final Polygon polygon = fir.getPolygon();

        c.setStroke(color);
        c.setFill(color);

        final Point2D polylabel = polygon.getPolylabel();
        if (text && polylabel != null) {
            c.setTextAlign(TextAlignment.CENTER);
            c.setTextBaseline(VPos.CENTER);
            c.fillText(
                    fir.icaoProperty().get(),
                    mapVariables.toCanvasX(polylabel.getX()),
                    mapVariables.toCanvasY(polylabel.getY())
            );
        }

        c.setLineWidth(lineWidth);
        c.setLineDashes(null);
        painterHelper.strokePolygons(c, polygon);

        if (fill) {
            c.setFill(color.deriveColor(0, 1, 1, 0.05));
            painterHelper.fillPolygons(c, polygon);
        }
    }
}
