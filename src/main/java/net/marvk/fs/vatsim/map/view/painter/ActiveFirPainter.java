package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;
import java.util.List;

public class ActiveFirPainter extends CompositeMapPainter<FlightInformationRegionBoundary> {
    @MetaPainter("FIR")
    private final FirPainter firPainter;

    public ActiveFirPainter(final MapVariables mapVariables) {
        this.firPainter = new FirPainter(
                mapVariables,
                Color.DARKMAGENTA,
                1,
                true,
                true,
                true
        );
    }

    @Override
    protected Collection<Painter<?>> painters() {
        return List.of(firPainter);
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