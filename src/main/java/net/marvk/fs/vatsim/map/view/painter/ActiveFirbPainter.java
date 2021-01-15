package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;
import java.util.List;

public class ActiveFirbPainter extends CompositeMapPainter<FlightInformationRegionBoundary> {
    @MetaPainter("FIR")
    private final FirbPainter firbPainter;

    public ActiveFirbPainter(final MapVariables mapVariables) {
        this.firbPainter = new FirbPainter(
                mapVariables,
                Color.DARKMAGENTA,
                1,
                true,
                true,
                true
        );
    }

    @Override
    protected Collection<? extends Painter<?>> painters() {
        return List.of(firbPainter);
    }

    @Override
    public void afterAllRender() {
        firbPainter.afterAllRender();
    }

    @Override
    public void paint(final GraphicsContext c, final FlightInformationRegionBoundary firb) {
        if (firb.hasFirControllers()) {
            firbPainter.paint(c, firb);
        }
    }
}