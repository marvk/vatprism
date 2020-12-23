package net.marvk.fs.vatsim.map.data;

import org.apache.commons.lang3.StringUtils;

public class SimplePredicatesDataVisitor extends DefaultingDataVisitor<Boolean> {
    private final String query;

    public SimplePredicatesDataVisitor(final String query) {
        super(false);
        this.query = query;
    }

    @Override
    public Boolean visit(final Controller controller) {
        return super.visit(controller) || StringUtils.containsIgnoreCase(controller.getFrequency(), query);
    }

    @Override
    public Boolean visit(final Airport airport) {
        return StringUtils.containsIgnoreCase(airport.getIcao(), query) ||
                airport
                        .getNames()
                        .stream()
                        .anyMatch(name -> StringUtils.containsIgnoreCase(name.get(), query));
    }

    @Override
    public Boolean visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return StringUtils.containsIgnoreCase(flightInformationRegionBoundary.getIcao(), query) ||
                flightInformationRegionBoundary
                        .getFlightInformationRegions()
                        .stream()
                        .map(FlightInformationRegion::getName)
                        .anyMatch(e -> StringUtils.containsIgnoreCase(e, query));
    }

    @Override
    public Boolean visit(final UpperInformationRegion upperInformationRegion) {
        return StringUtils.containsIgnoreCase(upperInformationRegion.getIcao(), query) ||
                StringUtils.containsIgnoreCase(upperInformationRegion.getName(), query);
    }

    @Override
    public Boolean visit(final Client client) {
        return StringUtils.containsIgnoreCase(client.getCallsign(), query) ||
                StringUtils.containsIgnoreCase(client.getRealName(), query) ||
                StringUtils.containsIgnoreCase(client.getCidString(), query);
    }

    public static DataVisitor<Boolean> nullOrBlankIsTrue(final String query) {
        if (query == null || query.isBlank()) {
            return new DefaultingDataVisitor<>(true);
        } else {
            return new SimplePredicatesDataVisitor(query);
        }
    }
}
