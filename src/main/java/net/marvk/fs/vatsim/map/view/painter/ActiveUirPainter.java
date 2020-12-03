package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class ActiveUirPainter implements Painter<UpperInformationRegion> {
    @MetaPainter("FIR")
    private final FirPainter firPainter;

    public ActiveUirPainter(final MapVariables mapVariables) {
        firPainter = new FirPainter(
                mapVariables,
                Color.DARKCYAN,
                0.5,
                true,
                true
        );
    }

    @Override
    public void afterAllRender() {
        firPainter.afterAllRender();
    }

    @Override
    public void paint(final GraphicsContext c, final UpperInformationRegion uir) {
        for (final FlightInformationRegionBoundary firb : uir.getFlightInformationRegionBoundaries()) {
            if (!firb.hasFirControllers() && firb.hasUirControllers()) {
                firPainter.paint(c, firb);
            }
        }
    }
}
