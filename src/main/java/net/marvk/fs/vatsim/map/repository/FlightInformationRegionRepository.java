package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.beans.property.StringProperty;
import lombok.Data;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimFlightInformationRegion;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionViewModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FlightInformationRegionRepository extends ProviderRepository<VatsimFlightInformationRegion, FlightInformationRegionViewModel> {
    @Inject
    public FlightInformationRegionRepository(final VatsimApi vatsimApi, final Provider<FlightInformationRegionViewModel> flightInformationRegionViewModelProvider) {
        super(vatsimApi, flightInformationRegionViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimFlightInformationRegion vatsimFlightInformationRegion) {
        final VatsimFlightInformationRegion f = vatsimFlightInformationRegion;
        return String.join("_", f.getIcao(), f.getName(), f.getPrefixPosition(), f.getUnknown1());
    }

    @Override
    protected Collection<VatsimFlightInformationRegion> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getFlightInformationRegions();
    }

    public List<FlightInformationRegionViewModel> getByIdentifierAndInfix(final String identifier, final String infix) {
        if (identifier == null) {
            return Collections.emptyList();
        }
        return byPrefix(list, identifier, infix);
    }

    private static List<FlightInformationRegionViewModel> byPrefix(final List<FlightInformationRegionViewModel> list, final String identifier, final String infix) {
        final QueryResult noInfix = query(list, identifier, FlightInformationRegionViewModel::prefixPositionProperty);
        if (noInfix.isOneResult()) {
            return noInfix.getResult();
        }
        if (noInfix.isManyResults()) {
            return byUnknown1(noInfix.getResult(), identifier, infix);
        }

        final QueryResult withInfix = query(list, identifier + "_" + infix, FlightInformationRegionViewModel::prefixPositionProperty);
        if (withInfix.isOneResult()) {
            return withInfix.getResult();
        }
        if (withInfix.isManyResults()) {
            return byUnknown1(withInfix.getResult(), identifier, infix);
        }

        return byUnknown1(list, identifier, infix);
    }

    private static List<FlightInformationRegionViewModel> byUnknown1(final List<FlightInformationRegionViewModel> list, final String identifier, final String infix) {
        final QueryResult noInfix = query(list, identifier, FlightInformationRegionViewModel::unknown1Property);
        if (noInfix.isOneResult()) {
            return noInfix.getResult();
        }
        if (noInfix.isManyResults()) {
            return byIcao(noInfix.getResult(), identifier, infix);
        }

        final QueryResult withInfix = query(list, identifier + "_" + infix, FlightInformationRegionViewModel::unknown1Property);
        if (withInfix.isOneResult()) {
            return withInfix.getResult();
        }
        if (withInfix.isManyResults()) {
            return byIcao(withInfix.getResult(), identifier, infix);
        }

        return byIcao(list, identifier, infix);
    }

    private static List<FlightInformationRegionViewModel> byIcao(final List<FlightInformationRegionViewModel> list, final String identifier, final String infix) {
        final QueryResult noInfix = query(list, identifier, FlightInformationRegionViewModel::icaoProperty);
        if (noInfix.isOneResult()) {
            return noInfix.getResult();
        }
        if (noInfix.isManyResults()) {
            return noInfix.getResult();
        }

        final QueryResult withInfix = query(list, identifier + "_" + infix, FlightInformationRegionViewModel::icaoProperty);
        if (withInfix.isOneResult()) {
            return withInfix.getResult();
        }
        if (withInfix.isManyResults()) {
            return withInfix.getResult();
        }

        return Collections.emptyList();
    }

    private static QueryResult query(final List<FlightInformationRegionViewModel> list, final String identifier, final Function<FlightInformationRegionViewModel, StringProperty> queryExtractor) {
        final List<FlightInformationRegionViewModel> byPrefixPosition = list
                .stream()
                .filter(e -> equalsIgnoreCaseAndDividers(queryExtractor.apply(e).get(), identifier))
                .collect(Collectors.toList());
        return new QueryResult(byPrefixPosition);
    }

    private static boolean equalsIgnoreCaseAndDividers(final String nullableString, final String string) {
        if (nullableString == null) {
            return false;
        }

        return nullableString.replaceAll("-", "_").equalsIgnoreCase(string.replaceAll("-", "_"));
    }

    @Data
    private static final class QueryResult {
        private final List<FlightInformationRegionViewModel> result;
        private final boolean noResult;
        private final boolean oneResult;
        private final boolean manyResults;

        private QueryResult(final List<FlightInformationRegionViewModel> result) {
            this.result = result;
            this.noResult = result.isEmpty();
            this.oneResult = result.size() == 1;
            this.manyResults = result.size() > 1;
        }
    }
}
