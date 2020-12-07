package net.marvk.fs.vatsim.map.view.painter;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class SelectedPainter extends CompositeMapPainter<Data> {
    @MetaPainter("FIR")
    private final FirPainter firPainter = new FirPainter(mapVariables, Color.RED, 2.5);

    @MetaPainter("Pilot")
    private final PilotPainter pilotPainter = new PilotPainter(mapVariables, Color.RED);

    @MetaPainter("Airport")
    private final AirportPainter airportPainter = new AirportPainter(mapVariables, Color.RED, Color.RED, true, true);

    private final PainterVisitor painterVisitor = new PainterVisitor();

    public SelectedPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext context, final Data data) {
        painterVisitor.visit(data).accept(context);
    }

    @Override
    protected Collection<Painter<?>> painters() {
        return List.of(firPainter);
    }

    private class PainterVisitor extends DefaultingDataVisitor<Consumer<GraphicsContext>> {
        public PainterVisitor() {
            super(c -> {
            });
        }

        @Override
        public Consumer<GraphicsContext> visit(final Airport airport) {
            return c -> airportPainter.paint(c, airport);
        }

        @Override
        public Consumer<GraphicsContext> visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
            return c -> firPainter.paint(c, flightInformationRegionBoundary);
        }

        @Override
        public Consumer<GraphicsContext> visit(final Pilot pilot) {
            return c -> pilotPainter.paint(c, pilot);
        }
    }
}
