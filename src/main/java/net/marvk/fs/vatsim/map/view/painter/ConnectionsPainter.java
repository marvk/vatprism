package net.marvk.fs.vatsim.map.view.painter;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.map.MapVariables;

import java.util.function.Consumer;

public class ConnectionsPainter extends MapPainter<Data> {

    @Parameter("History")
    private boolean history = true;

    @Parameter("Departure")
    private boolean departure = true;
    @Parameter("Departure Color")
    private Color departureColor = Color.CORAL.deriveColor(0, 1, 1, 0.5);

    @Parameter("Arrival")
    private boolean arrival = true;
    @Parameter("Arrival Color")
    private Color arrivalColor = Color.DARKOLIVEGREEN.deriveColor(0, 1, 1, 0.5);

    @Parameter("Great Circle Lines")
    private boolean greatCircle = true;

    private final PainterVisitor painterVisitor = new PainterVisitor();

    private Point2D[] greatCircleBufferArray = null;

    public ConnectionsPainter(final MapVariables mapVariables) {
        super(mapVariables);
    }

    @Override
    public void paint(final GraphicsContext context, final Data data) {
        painterVisitor.visit(data).accept(context);
    }

    private static int airportOffsetX(final Pilot pilot, final Airport airport) {
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

    private void greatCircleLine(final GraphicsContext c, final Pilot pilot, final Point2D airportPosition, final Type type) {
        if (type == Type.ARRIVAL || !history) {
            final Point2D[] points = GeomUtil.greatCirclePolyline(pilot.getPosition(), airportPosition, getGreatCircleBufferArray());
            painterHelper.strokePolyline(c, points);
        } else if (type == Type.DEPARTURE) {
            final Point2D[] points = GeomUtil.greatCirclePolyline(pilot.getHistory()
                                                                       .get(0), airportPosition, getGreatCircleBufferArray());
            painterHelper.strokePolyline(c, points);
            c.setLineDashes(null);
            painterHelper.strokePolyline(c, pilot.getHistory().toArray(Point2D[]::new));
        }

    }

    private Point2D[] getGreatCircleBufferArray() {
        if (greatCircleBufferArray == null) {
            greatCircleBufferArray = new Point2D[51];
        }

        return greatCircleBufferArray;
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

        private void paint(final GraphicsContext context, final Pilot pilot, final Airport airport, final Type type) {
            if (type == Type.ARRIVAL && arrival) {
                context.setLineDashes(1, 5);
                context.setStroke(arrivalColor);
            } else if (type == Type.DEPARTURE && departure) {
                context.setLineDashes(1, 10);
                context.setStroke(departureColor);
            } else {
                return;
            }

            if (pilot == null || airport == null || pilot.getPosition() == null || airport.getPosition() == null) {
                return;
            }

            final Point2D airportPosition = airport.getPosition().add(airportOffsetX(pilot, airport), 0);

            if (greatCircle) {
                greatCircleLine(context, pilot, airportPosition, type);
            } else {
                line(context, pilot.getPosition(), airportPosition, 0);
                line(context, pilot.getPosition(), airportPosition, (int) (Math.signum(pilot.getPosition()
                                                                                            .getX()) * -360));
            }
        }
    }
}