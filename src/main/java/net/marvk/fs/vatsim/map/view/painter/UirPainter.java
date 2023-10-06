package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;
import java.util.List;

public class UirPainter extends CompositeMapPainter<UpperInformationRegion> {
    @MetaPainter(name = "Flight Information Regions", legacyName = "FIR")
    private final FirbPainter firbPainter;

    @Parameter(name = "Show FIRs that have a non-UIR controller", legacyName = "Paint FIRs with FIR controller")
    private boolean paintFirControlled;

    public UirPainter(final MapVariables mapVariables, final Color color, final boolean paintFirControlled) {
        this.firbPainter = new FirbPainter(
                mapVariables,
                color,
                0.5,
                true,
                true,
                true
        );

        this.paintFirControlled = paintFirControlled;
    }

    @Override
    protected Collection<? extends Painter<?>> painters() {
        return List.of(firbPainter);
    }

    @Override
    public void paint(final GraphicsContext c, final UpperInformationRegion uir) {
        for (final FlightInformationRegionBoundary firb : uir.getFlightInformationRegionBoundaries()) {
            if ((!firb.hasFirControllers() || paintFirControlled)) {
                firbPainter.paint(c, firb);
            }
        }
    }
}
