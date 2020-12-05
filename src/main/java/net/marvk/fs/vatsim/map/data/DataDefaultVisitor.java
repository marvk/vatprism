package net.marvk.fs.vatsim.map.data;

import java.util.Objects;

public abstract class DataDefaultVisitor<E> implements DataVisitor<E> {
    private final E defaultValue;

    public DataDefaultVisitor(final E defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    @Override
    public E visit(final Airport airport) {
        return defaultValue;
    }

    @Override
    public E visit(final Controller controller) {
        return defaultValue;
    }

    @Override
    public E visit(final FlightInformationRegion flightInformationRegion) {
        return defaultValue;
    }

    @Override
    public E visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return defaultValue;
    }

    @Override
    public E visit(final FlightPlan flightPlan) {
        return defaultValue;
    }

    @Override
    public E visit(final InternationalDateLine internationalDateLine) {
        return defaultValue;
    }

    @Override
    public E visit(final Pilot pilot) {
        return defaultValue;
    }

    @Override
    public E visit(final UpperInformationRegion upperInformationRegion) {
        return defaultValue;
    }
}
