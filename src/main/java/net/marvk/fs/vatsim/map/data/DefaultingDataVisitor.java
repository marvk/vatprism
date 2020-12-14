package net.marvk.fs.vatsim.map.data;

import java.util.Objects;

public class DefaultingDataVisitor<E> implements DataVisitor<E> {
    private final E defaultValue;

    public DefaultingDataVisitor(final E defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    @Override
    public E visit(final Data data) {
        if (data != null) {
            return data.visit(this);
        }

        return defaultValue;
    }

    @Override
    public E visit(final Airport airport) {
        return defaultValue;
    }

    @Override
    public E visit(final Controller controller) {
        return visit((Client) controller);
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
        return visit((Client) pilot);
    }

    @Override
    public E visit(final UpperInformationRegion upperInformationRegion) {
        return defaultValue;
    }

    @Override
    public E visit(final Atis atis) {
        return visit((Controller) atis);
    }

    @Override
    public E visit(final Client client) {
        return defaultValue;
    }
}
