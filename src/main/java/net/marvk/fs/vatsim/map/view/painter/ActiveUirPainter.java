package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;
import java.util.List;

public class ActiveUirPainter extends CompositeMapPainter<UpperInformationRegion> {
    @MetaPainter("FIR")
    private final FirbPainter firbPainter;

    public ActiveUirPainter(final MapVariables mapVariables) {
        this.firbPainter = new FirbPainter(
                mapVariables,
                Color.DARKCYAN,
                0.5,
                true,
                true,
                true
        );
    }

    @Override
    protected Collection<Painter<?>> painters() {
        return List.of(firbPainter);
    }

    @Override
    public void afterAllRender() {
        firbPainter.afterAllRender();
    }

    @Override
    public void paint(final GraphicsContext c, final UpperInformationRegion uir) {
        for (final FlightInformationRegionBoundary firb : uir.getFlightInformationRegionBoundaries()) {
            if (!firb.hasFirControllers() && firb.hasUirControllers()) {
                firbPainter.paint(c, firb);
            }
        }
    }
}
