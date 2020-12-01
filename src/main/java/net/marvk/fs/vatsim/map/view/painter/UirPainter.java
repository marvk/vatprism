package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class UirPainter extends MapPainter<UpperInformationRegion> {
    private final Painter<FlightInformationRegionBoundary> firPainter;

    public UirPainter(final MapVariables mapVariables, final Painter<FlightInformationRegionBoundary> firPainter) {
        super(mapVariables);
        this.firPainter = firPainter;
    }

    @Override
    public void paint(final GraphicsContext c, final UpperInformationRegion upperInformationRegionViewModel) {
        for (final FlightInformationRegionBoundary fir : upperInformationRegionViewModel.getFlightInformationRegionBoundaries()) {
            firPainter.paint(c, fir);
        }
    }
}
