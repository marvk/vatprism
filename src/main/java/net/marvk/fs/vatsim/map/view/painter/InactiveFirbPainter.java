package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class InactiveFirbPainter extends MapPainter<FlightInformationRegionBoundary> {
    @MetaPainter(name = "Flight Information Regions", legacyName = "FIR")
    private final FirbPainter firbPainter;

    public InactiveFirbPainter(final MapVariables mapVariables) {
        super(mapVariables);

        firbPainter = new FirbPainter(
                mapVariables,
                Color.valueOf("3B341F").deriveColor(0, 1, 1, 0.25),
                0.5,
                false,
                true,
                false
        );
        enabled = false;
    }

    @Override
    public void afterAllRender() {
        firbPainter.afterAllRender();
    }

    @Override
    public void paint(final GraphicsContext c, final FlightInformationRegionBoundary firb) {
        if (firb.hasFirControllers() || firb.hasUirControllers()) {
            return;
        }

        firbPainter.paint(c, firb);
    }
}
