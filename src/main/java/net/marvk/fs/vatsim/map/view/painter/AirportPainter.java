package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.AirportViewModel;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class AirportPainter extends MapPainter<AirportViewModel> {
    public AirportPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final Canvas canvas, final AirportViewModel airportViewModel) {
        //        final Color color = Color.DARKGRAY;
        final Point2D point = airportViewModel.getPosition();
        final int x = (int) mapVariables.toCanvasX(point.getX());
        final int y = (int) mapVariables.toCanvasY(point.getY());

        painterHelper.setPixel(canvas.getGraphicsContext2D(), Color.GREY, x, y);

//            c.setFill(Color.WHEAT);
//            c.fillText(airport.icaoProperty().get(), x, y);
    }
}
