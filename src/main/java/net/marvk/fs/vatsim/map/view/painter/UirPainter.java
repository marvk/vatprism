package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.Canvas;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryViewModel;
import net.marvk.fs.vatsim.map.data.UpperInformationRegionViewModel;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

public class UirPainter extends MapPainter<UpperInformationRegionViewModel> {
    private final Painter<FlightInformationRegionBoundaryViewModel> firPainter;

    public UirPainter(final MapVariables mapVariables, final Painter<FlightInformationRegionBoundaryViewModel> firPainter) {
        super(mapVariables);
        this.firPainter = firPainter;
    }

    @Override
    public void paint(final Canvas canvas, final UpperInformationRegionViewModel upperInformationRegionViewModel) {
        for (final var fir : upperInformationRegionViewModel.flightInformationRegionBoundaries()) {
            firPainter.paint(canvas, fir);
        }
    }
}
