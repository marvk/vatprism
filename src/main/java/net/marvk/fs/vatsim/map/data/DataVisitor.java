package net.marvk.fs.vatsim.map.data;

import java.util.Optional;

public interface DataVisitor<E> {
    default E visit(Data data) {
        return data.visit(this);
    }

    default Optional<E> visitNullSafe(final Data data) {
        if (data == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(visit(data));
    }

    default E visit(final Airport airport) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final Controller controller) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final FlightInformationRegion flightInformationRegion) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final FlightPlan flightPlan) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final InternationalDateLine internationalDateLine) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final Pilot pilot) {
        throw new UnsupportedOperationException("Not implemented");
    }

    default E visit(final UpperInformationRegion upperInformationRegion) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
