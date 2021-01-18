package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import lombok.Value;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ConnectionsPainter extends CompositeMapPainter<Data> {
    @Parameter("Departure Color")
    private Color departureColor = Color.CORAL.deriveColor(0, 1, 1, 0.5);

    @Parameter("Arrival Color")
    private Color arrivalColor = Color.DARKOLIVEGREEN.deriveColor(0, 1, 1, 0.5);

    @Parameter("Great Circle Lines")
    private boolean greatCircle = true;

    @MetaPainter("Pilots")
    private final ConnectionPainter pilots;

    @MetaPainter("Airport Departures")
    private final ConnectionPainter airportDepartures;

    @MetaPainter("Airport Arrivals")
    private final ConnectionPainter airportArrivals;

    private final PainterVisitor painterVisitor = new PainterVisitor();

    private Point2D[] greatCircleBufferArray = null;

    public ConnectionsPainter(final MapVariables mapVariables) {
        pilots = new ConnectionPainter(mapVariables, true, true, true);
        airportArrivals = new ConnectionPainter(mapVariables, false, false, true);
        airportDepartures = new ConnectionPainter(mapVariables, true, true, false);
    }

    @Override
    protected Collection<? extends Painter<?>> painters() {
        return List.of(
                pilots,
                airportDepartures,
                airportArrivals
        );
    }

    @Override
    public void paint(final GraphicsContext context, final Data data) {
        painterVisitor.visit(data).accept(context);
    }

    private Point2D[] getGreatCircleBufferArray() {
        if (greatCircleBufferArray == null) {
            greatCircleBufferArray = new Point2D[51];
        }

        return greatCircleBufferArray;
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
                    airportDepartures.paint(c, new Connection(
                            flightPlan.getDepartureAirport(),
                            flightPlan.getPilot(),
                            flightPlan.getArrivalAirport()
                    ));
                }

                for (final FlightPlan flightPlan : airport.getArriving()) {
                    airportArrivals.paint(c, new Connection(
                            flightPlan.getDepartureAirport(),
                            flightPlan.getPilot(),
                            flightPlan.getArrivalAirport()
                    ));
                }
            };
        }

        @Override
        public Consumer<GraphicsContext> visit(final Pilot pilot) {
            return c -> pilots.paint(c, new Connection(
                    pilot.getFlightPlan().getDepartureAirport(),
                    pilot,
                    pilot.getFlightPlan().getArrivalAirport()
            ));
        }
    }

    private class ConnectionPainter extends MapPainter<Connection> {
        @Parameter("History")
        private boolean history;

        @Parameter("Departure")
        private boolean departure;

        @Parameter("Arrival")
        private boolean arrival;

        public ConnectionPainter(final MapVariables mapVariables, final boolean history, final boolean departure, final boolean arrival) {
            super(mapVariables);
            this.history = history;
            this.departure = departure;
            this.arrival = arrival;
        }

        @Override
        public void paint(final GraphicsContext c, final Connection connection) {
            if (!enabled) {
                return;
            }

            final Airport departureAirport = connection.getDeparture();
            final Pilot pilot = connection.getPilot();
            final Airport arrivalAirport = connection.getArrival();

            if (arrival && arrivalAirport != null) {
                setArrivalStroke(c);
                connect(c, pilot.getPosition(), arrivalAirport.getPosition());
            }

            if (history) {
                setHistoryStroke(c);
                painterHelper.strokePolyline(c, pilot.getHistory().toArray(Point2D[]::new));
            }

            if (departure && departureAirport != null) {
                setDepartureStroke(c);
                if (history) {
                    connect(c, departureAirport.getPosition(), pilot.getHistory().get(0));
                } else {
                    connect(c, departureAirport.getPosition(), pilot.getPosition());
                }
            }
        }

        private void setArrivalStroke(final GraphicsContext c) {
            c.setLineDashes(1, 5);
            c.setStroke(arrivalColor);
        }

        private void setDepartureStroke(final GraphicsContext c) {
            c.setLineDashes(1, 10);
            c.setStroke(departureColor);
        }

        private void setHistoryStroke(final GraphicsContext c) {
            c.setLineDashes(null);
            c.setStroke(departureColor);
        }

        private void connect(final GraphicsContext c, final Point2D p1, final Point2D p2) {
            if (greatCircle) {
                greatCircleLine(c, p1, p2);
            } else {
                line(c, p1, p2, 0);
                line(c, p1, p2, (int) (Math.signum(p2.getX()) * -360));
            }
        }

        private void greatCircleLine(final GraphicsContext c, final Point2D p1, final Point2D p2) {
            final Point2D[] points = GeomUtil.greatCirclePolyline(p1, p2, getGreatCircleBufferArray());
            painterHelper.strokePolyline(c, points);
        }

        private void line(final GraphicsContext c, final Point2D p1, final Point2D p2, final int offsetX) {
            final Point2D c1 = mapVariables.toCanvas(p1.add(offsetX, 0));
            final Point2D c2 = mapVariables.toCanvas(p2.add(offsetX, 0));
            painterHelper.strokeLine(c, c1.getX(), c1.getY(), c2.getX(), c2.getY());
        }

    }

    @Value
    private static class Connection {
        Airport departure;
        Pilot pilot;
        Airport arrival;
    }
}
