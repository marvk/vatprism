package net.marvk.fs.vatsim.map.data;

public interface NullDataVisitor<E> extends DataVisitor<E> {
    @Override
    default E visit(final Data data) {
        if (data == null) {
            return null;
        }

        return data.visit(this);
    }

    @Override
    default E visit(final Airport airport) {
        return null;
    }

    @Override
    default E visit(final Controller controller) {
        return visit((Client) controller);
    }

    @Override
    default E visit(final FlightInformationRegion flightInformationRegion) {
        return null;
    }

    @Override
    default E visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return null;
    }

    @Override
    default E visit(final FlightPlan flightPlan) {
        return null;
    }

    @Override
    default E visit(final InternationalDateLine internationalDateLine) {
        return null;
    }

    @Override
    default E visit(final Pilot pilot) {
        return visit((Client) pilot);
    }

    @Override
    default E visit(final UpperInformationRegion upperInformationRegion) {
        return null;
    }

    @Override
    default E visit(final Atis atis) {
        return visit((Controller) atis);
    }

    @Override
    default E visit(final Client client) {
        return null;
    }
}
