package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.function.Consumer;

public class DepartureArrivalPathPainter extends MapPainter<Data> {
    @Parameter("Departure")
    private final boolean departure = true;
    @Parameter("Departure Color")
    private final Color departureColor = Color.CORAL.deriveColor(0, 1, 1, 0.5);

    @Parameter("Arrival")
    private final boolean arrival = true;
    @Parameter("Arrival Color")
    private final Color arrivalColor = Color.DARKOLIVEGREEN.deriveColor(0, 1, 1, 0.5);

    private final PainterVisitor painterVisitor = new PainterVisitor();

    public DepartureArrivalPathPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext context, final Data data) {
        painterVisitor.visit(data).accept(context);
    }

    private void paint(final GraphicsContext context, final Pilot pilot, final Airport airport, final Type type) {
        if (type == Type.ARRIVAL) {
            context.setLineDashes(1, 5);
            context.setStroke(arrivalColor);
        } else if (type == Type.DEPARTURE) {
            context.setLineDashes(1, 10);
            context.setStroke(departureColor);
        } else {
            return;
        }

        if (pilot == null || airport == null || pilot.getPosition() == null || airport.getPosition() == null) {
            return;
        }

        final Point2D airportPosition = airport.getPosition().add(airportOffsetX(pilot, airport), 0);

        line(context, pilot.getPosition(), airportPosition, 0);
        line(context, pilot.getPosition(), airportPosition, (int) (Math.signum(pilot.getPosition().getX()) * -360));
    }

    private int airportOffsetX(final Pilot pilot, final Airport airport) {
        if (Math.abs(pilot.getPosition().getX() - airport.getPosition().getX()) > 180) {
            if (pilot.getPosition().getX() < 0) {
                return -360;
            } else {
                return 360;
            }
        } else {
            return 0;
        }
    }

    private void line(final GraphicsContext c, final Point2D p1, final Point2D p2, final int offsetX) {
        final Point2D c1 = mapVariables.toCanvas(p1.add(offsetX, 0));
        final Point2D c2 = mapVariables.toCanvas(p2.add(offsetX, 0));
        painterHelper.strokeLine(c, c1.getX(), c1.getY(), c2.getX(), c2.getY());
    }

    private enum Type {
        DEPARTURE, ARRIVAL, ALTERNATE;
    }

    private class PainterVisitor extends DefaultingDataVisitor<Consumer<GraphicsContext>> {
        public PainterVisitor() {
            super(c -> {
            });
        }

        @Override
        public Consumer<GraphicsContext> visit(final Airport airport) {
            return c -> {
                for (final FlightPlan flightPlan : airport.getDeparting()) {
                    paint(c, flightPlan.getPilot(), flightPlan.getDepartureAirport(), Type.DEPARTURE);
                }

                for (final FlightPlan flightPlan : airport.getArriving()) {
                    paint(c, flightPlan.getPilot(), flightPlan.getArrivalAirport(), Type.ARRIVAL);
                }
            };
        }

        @Override
        public Consumer<GraphicsContext> visit(final Pilot pilot) {
            return c -> {
                final FlightPlan flightPlan = pilot.getFlightPlan();
                paint(c, flightPlan.getPilot(), flightPlan.getArrivalAirport(), Type.ARRIVAL);
                paint(c, flightPlan.getPilot(), flightPlan.getDepartureAirport(), Type.DEPARTURE);
            };
        }
    }
}
