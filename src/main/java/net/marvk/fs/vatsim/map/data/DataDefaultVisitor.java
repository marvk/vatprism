package net.marvk.fs.vatsim.map.data;

import java.util.Optional;

public abstract class DataDefaultVisitor<E> implements DataVisitor<Optional<E>> {
    private final Optional<E> defaultValue;

    public DataDefaultVisitor(final E defaultValue) {
        this.defaultValue = Optional.ofNullable(defaultValue);
    }

    public DataDefaultVisitor() {
        this(null);
    }

    @Override
    public Optional<E> visit(final Airport airport) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final Controller controller) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final FlightInformationRegion flightInformationRegion) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final FlightPlan flightPlan) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final InternationalDateLine internationalDateLine) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final Pilot pilot) {
        return defaultValue;
    }

    @Override
    public Optional<E> visit(final UpperInformationRegion upperInformationRegion) {
        return defaultValue;
    }
}
