package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class ActiveFirPainter implements Painter<FlightInformationRegionBoundary> {
    private final FirPainter firPainter;

    public ActiveFirPainter(final MapVariables mapVariables) {
        firPainter = new FirPainter(
                mapVariables,
                Color.DARKMAGENTA,
                1,
                true,
                true
        );
    }

    @Override
    public void afterAllRender() {
        firPainter.afterAllRender();
    }

    @Override
    public void paint(final GraphicsContext c, final FlightInformationRegionBoundary firb) {
        if (firb.hasFirControllers()) {
            firPainter.paint(c, firb);
        }
    }
}