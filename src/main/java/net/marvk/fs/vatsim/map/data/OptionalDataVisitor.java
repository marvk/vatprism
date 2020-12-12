package net.marvk.fs.vatsim.map.data;

import java.util.Optional;

public interface OptionalDataVisitor<E> extends NullDataVisitor<Optional<E>> {
    @Override
    default Optional<E> visit(final Data data) {
        if (data == null) {
            return Optional.empty();
        }

        return data.visit(this);
    }

    @Override
    default Optional<E> visit(final Airport airport) {
        return Optional.empty();
    }

    @Override
    default Optional<E> visit(final Controller controller) {
        return visit((Client) controller);
    }

    @Override
    default Optional<E> visit(final FlightInformationRegion flightInformationRegion) {
        return Optional.empty();
    }

    @Override
    default Optional<E> visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return Optional.empty();
    }

    @Override
    default Optional<E> visit(final FlightPlan flightPlan) {
        return Optional.empty();
    }

    @Override
    default Optional<E> visit(final InternationalDateLine internationalDateLine) {
        return Optional.empty();
    }

    @Override
    default Optional<E> visit(final Pilot pilot) {
        return visit((Client) pilot);
    }

    @Override
    default Optional<E> visit(final UpperInformationRegion upperInformationRegion) {
        return Optional.empty();
    }

    @Override
    default Optional<E> visit(final Atis atis) {
        return visit(((Controller) atis));
    }

    @Override
    default Optional<E> visit(final Client client) {
        return Optional.empty();
    }
}
