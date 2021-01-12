package net.marvk.fs.vatsim.map.data;

import javafx.beans.value.ObservableObjectValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class SimplePredicatesDataVisitor extends DefaultingDataVisitor<Boolean> {
    private final String query;

    public SimplePredicatesDataVisitor(final String query) {
        super(false);
        this.query = query.toLowerCase(Locale.ROOT);
    }

    private boolean matches(final String s) {
        return StringUtils.containsIgnoreCase(s, query);
    }

    @Override
    public Boolean visit(final Controller controller) {
        return super.visit(controller) || matches(controller.getFrequency());
    }

    @Override
    public Boolean visit(final Airport airport) {
        return matches(airport.getIcao()) ||
                airport
                        .getNames()
                        .stream()
                        .map(ObservableObjectValue::get)
                        .anyMatch(this::matches);
    }

    @Override
    public Boolean visit(final FlightInformationRegionBoundary flightInformationRegionBoundary) {
        return matches(flightInformationRegionBoundary.getIcao()) ||
                flightInformationRegionBoundary
                        .getFlightInformationRegions()
                        .stream()
                        .map(FlightInformationRegion::getName)
                        .anyMatch(this::matches);
    }

    @Override
    public Boolean visit(final UpperInformationRegion upperInformationRegion) {
        return matches(upperInformationRegion.getIcao()) ||
                matches(upperInformationRegion.getName());
    }

    @Override
    public Boolean visit(final Client client) {
        return matches(client.getCallsign()) ||
                matches(client.getRealName()) ||
                matches(client.getCidString());
    }

    public static DataVisitor<Boolean> nullOrBlankIsTrue(final String query) {
        if (query == null || query.isBlank()) {
            return new DefaultingDataVisitor<>(true);
        } else {
            return new SimplePredicatesDataVisitor(query);
        }
    }
}
