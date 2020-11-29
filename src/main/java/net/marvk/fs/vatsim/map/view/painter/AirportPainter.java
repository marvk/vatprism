package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.AirportViewModel;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class AirportPainter extends MapPainter<AirportViewModel> {
    public AirportPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext c, final AirportViewModel airportViewModel) {
        //        final Color color = Color.DARKGRAY;
        final Point2D point = airportViewModel.getPosition();
        final int x = (int) mapVariables.toCanvasX(point.getX());
        final int y = (int) mapVariables.toCanvasY(point.getY());

        painterHelper.setPixel(c, Color.GREY, x, y);

//            c.setFill(Color.WHEAT);
//            c.fillText(airport.icaoProperty().get(), x, y);
    }
}
