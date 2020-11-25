package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.Polygon;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class FirPainter extends MapPainter<FlightInformationRegionBoundaryViewModel> {

    private final Color borderColor;
    private final double lineWidth;

    public FirPainter(final MapVariables mapVariables, final Color borderColor, final double lineWidth) {
        super(mapVariables);
        this.borderColor = borderColor;
        this.lineWidth = lineWidth;
    }

    @Override
    public void paint(final Canvas canvas, final FlightInformationRegionBoundaryViewModel flightInformationRegionBoundaryViewModel) {
//        final Color activeFirColor = Color.valueOf("#3B341F");

        final GraphicsContext c = canvas.getGraphicsContext2D();

        c.setLineWidth(lineWidth);
        c.setStroke(borderColor);

        final FlightInformationRegionBoundaryViewModel fir = flightInformationRegionBoundaryViewModel;
        if (fir.extensionProperty().get()) {
            return;
        }
        final Polygon polygon = fir.getPolygon();

//            if (i == y) {
//                c.setLineWidth(3);
//                final Point2D min = polygon.getMin();
//                final Point2D max = polygon.getMax();
//
//                System.out.println(fir.icaoProperty().get());
//                System.out.println("min = " + min);
//                System.out.println("max = " + max);
//
//                final double x1 = toCanvasX(min.getX());
//                final double y1 = toCanvasY(max.getY());
//                final double x2 = toCanvasX(max.getX());
//                final double y2 = toCanvasY(min.getY());
//
//                c.setStroke(Color.RED);
//                c.strokeRect(
//                        x1,
//                        y1,
//                        x2 - x1,
//                        y2 - y1
//                );
//
//                c.setStroke(Color.CYAN);
//            }

        painterHelper.strokePolygons(c, polygon);
    }
}
