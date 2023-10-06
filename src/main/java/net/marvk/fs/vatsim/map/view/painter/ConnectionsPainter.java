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
import java.util.function.Supplier;

public class ConnectionsPainter extends CompositeMapPainter<Data> {
    @Parameter(name = "Stroke Width", min = 0, max = 10)
    private double strokeWidth = 1;

    @Parameter(name = "Departure Color")
    private Color departureColor = Color.CORAL.deriveColor(0, 1, 1, 0.5);

    @Parameter(name = "Arrival Color")
    private Color arrivalColor = Color.DARKOLIVEGREEN.deriveColor(0, 1, 1, 0.5);

    @Parameter(name = "Great Circle Lines", hintText = "When enabled, shows tracks as great circle lines. When disabled, shows tracks as straight lines on the map.")
    private boolean greatCircle = true;

    @MetaPainter(name = "Flights", legacyName = "Pilots")
    private final ConnectionPainter pilots;

    @MetaPainter(name = "Airport Departures")
    private final ConnectionPainter airportDepartures;

    @MetaPainter(name = "Airport Arrivals")
    private final ConnectionPainter airportArrivals;

    private final PainterVisitor painterVisitor = new PainterVisitor();

    private Point2D[] greatCircleBufferArray = null;

    public ConnectionsPainter(final MapVariables mapVariables) {
        pilots = new ConnectionPainter(
                mapVariables,
                true,
                true,
                true,
                () -> departureColor,
                () -> arrivalColor
        );
        airportArrivals = new ConnectionPainter(
                mapVariables,
                false,
                false,
                true,
                () -> arrivalColor,
                () -> arrivalColor
        );
        airportDepartures = new ConnectionPainter(
                mapVariables,
                true,
                true,
                false,
                () -> departureColor,
                () -> departureColor
        );
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
    public void paint(final GraphicsContext c, final Data data) {
        c.setLineWidth(strokeWidth);
        painterVisitor.visit(data).accept(c);
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
        @Parameter(name = "Show History in Departure Track", legacyName = "History", hintText = "When enabled, shows the exact path the aircraft has taken since application start")
        private boolean history;

        @Parameter(name = "Show Track to Departure Airport", legacyName = "Departure")
        private boolean departure;

        @Parameter(name = "Show Track to Arrival Airport", legacyName = "Arrival")
        private boolean arrival;

        private final Supplier<Color> departureColorSupplier;
        private final Supplier<Color> arrivalColorSupplier;

        public ConnectionPainter(
                final MapVariables mapVariables,
                final boolean history,
                final boolean departure,
                final boolean arrival,
                final Supplier<Color> departureColorSupplier,
                final Supplier<Color> arrivalColorSupplier
        ) {
            super(mapVariables);
            this.history = history;
            this.departure = departure;
            this.arrival = arrival;
            this.departureColorSupplier = departureColorSupplier;
            this.arrivalColorSupplier = arrivalColorSupplier;
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
            c.setStroke(arrivalColorSupplier.get());
        }

        private void setDepartureStroke(final GraphicsContext c) {
            c.setLineDashes(1, 10);
            c.setStroke(departureColorSupplier.get());
        }

        private void setHistoryStroke(final GraphicsContext c) {
            c.setLineDashes(null);
            c.setStroke(departureColorSupplier.get());
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
