package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class InactiveFirPainter extends MapPainter<FlightInformationRegionBoundary> {
    private final FirPainter firPainter;

    public InactiveFirPainter(final MapVariables mapVariables) {
        super(mapVariables);

        firPainter = new FirPainter(
                mapVariables,
                Color.valueOf("3B341F").deriveColor(0, 1, 1, 0.25),
                0.5,
                false,
                false
        );
    }

    @Override
    public void afterAllRender() {
        firPainter.afterAllRender();
    }

    @Override
    public void paint(final GraphicsContext c, final FlightInformationRegionBoundary firb) {
        if (firb.hasFirControllers() || firb.hasUirControllers()) {
            return;
        }

        firPainter.paint(c, firb);
    }
}
