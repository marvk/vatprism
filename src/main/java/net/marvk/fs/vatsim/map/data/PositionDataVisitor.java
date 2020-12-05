package net.marvk.fs.vatsim.map.data;

import javafx.geometry.Point2D;

import java.util.Optional;

public class PositionDataVisitor implements OptionalDataVisitor<Point2D> {
    @Override
    public Optional<Point2D> visit(final Airport airport) {
        return Optional.ofNullable(airport.getPosition());
    }

    @Override
    public Optional<Point2D> visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return Optional.ofNullable(flightInformationRegionBoundary.getPolygon().getPolyLabel());
    }

    @Override
    public Optional<Point2D> visit(final Pilot pilot) {
        return Optional.of(pilot.getPosition());
    }
}
