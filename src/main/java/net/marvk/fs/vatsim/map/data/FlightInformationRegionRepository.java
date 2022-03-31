package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.beans.property.ReadOnlyStringProperty;
import lombok.Data;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimFlightInformationRegion;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FlightInformationRegionRepository extends ProviderRepository<FlightInformationRegion, VatsimFlightInformationRegion> {
    private final Lookup<FlightInformationRegion> unknownLookup = Lookup.fromProperty(FlightInformationRegion::getUnknown1);
    private final Lookup<FlightInformationRegion> icaoLookup = Lookup.fromProperty(FlightInformationRegion::getIcao);

    @Inject
    public FlightInformationRegionRepository(
            final VatsimApi vatsimApi,
            final Provider<FlightInformationRegion> provider
    ) {
        super(vatsimApi, provider);
    }

    @Override
    protected String keyFromModel(final VatsimFlightInformationRegion vatsimFlightInformationRegion) {
        return String.join("_",
                vatsimFlightInformationRegion.getIcao(),
                vatsimFlightInformationRegion.getName(),
                vatsimFlightInformationRegion.getPrefixPosition(),
                vatsimFlightInformationRegion.getUnknown1()
        );
    }

    @Override
    protected String keyFromViewModel(final FlightInformationRegion flightInformationRegion) {
        return String.join("_",
                flightInformationRegion.getIcao(),
                flightInformationRegion.getName(),
                flightInformationRegion.getPrefixPosition(),
                flightInformationRegion.getUnknown1().replaceAll("_", "-")
        );
    }

    @Override
    protected Collection<VatsimFlightInformationRegion> extractModels(final VatsimApi api) throws VatsimApiException {
        return api.vatSpy().getFlightInformationRegions();
    }

    @Override
    protected void onAdd(final FlightInformationRegion toAdd, final VatsimFlightInformationRegion vatsimFlightInformationRegion) {
        icaoLookup.put(toAdd);
        unknownLookup.put(toAdd);
    }

    public List<FlightInformationRegion> getByIcao(final String icao) {
        return icaoLookup.get(icao);
    }

    public List<FlightInformationRegion> getByUnknown(final String unknown) {
        return unknownLookup.get(unknown);
    }

    public List<FlightInformationRegion> getByIdentifierAndInfix(final String identifier, final String infix) {
        if (identifier == null) {
            return Collections.emptyList();
        }
        return byPrefix(list(), identifier, infix);
    }

    @SuppressWarnings("DuplicatedCode")
    private static List<FlightInformationRegion> byPrefix(final List<FlightInformationRegion> list, final String identifier, final String infix) {
        final QueryResult withInfix = query(list, identifier + "_" + infix, FlightInformationRegion::prefixPositionProperty);
        if (withInfix.isOneResult()) {
            return withInfix.getResult();
        }
        if (withInfix.isManyResults()) {
            return byUnknown1(withInfix.getResult(), identifier, infix);
        }

        final QueryResult noInfix = query(list, identifier, FlightInformationRegion::prefixPositionProperty);
        if (noInfix.isOneResult()) {
            return noInfix.getResult();
        }
        if (noInfix.isManyResults()) {
            return byUnknown1(noInfix.getResult(), identifier, infix);
        }

        return byUnknown1(list, identifier, infix);
    }

    @SuppressWarnings("DuplicatedCode")
    private static List<FlightInformationRegion> byUnknown1(final List<FlightInformationRegion> list, final String identifier, final String infix) {
        final QueryResult withInfix = query(list, identifier + "_" + infix, FlightInformationRegion::unknown1Property);
        if (withInfix.isOneResult()) {
            return withInfix.getResult();
        }
        if (withInfix.isManyResults()) {
            return byIcao(withInfix.getResult(), identifier, infix);
        }

        final QueryResult noInfix = query(list, identifier, FlightInformationRegion::unknown1Property);
        if (noInfix.isOneResult()) {
            return noInfix.getResult();
        }
        if (noInfix.isManyResults()) {
            return byIcao(noInfix.getResult(), identifier, infix);
        }

        return byIcao(list, identifier, infix);
    }

    private static List<FlightInformationRegion> byIcao(final List<FlightInformationRegion> list, final String identifier, final String infix) {
        final QueryResult withInfix = query(list, identifier + "_" + infix, FlightInformationRegion::icaoProperty);
        if (withInfix.isOneResult()) {
            return withInfix.getResult();
        }
        if (withInfix.isManyResults()) {
            return withInfix.getResult();
        }

        final QueryResult noInfix = query(list, identifier, FlightInformationRegion::icaoProperty);
        if (noInfix.isOneResult()) {
            return noInfix.getResult();
        }
        if (noInfix.isManyResults()) {
            return noInfix.getResult();
        }

        return Collections.emptyList();
    }

    private static FlightInformationRegionRepository.QueryResult query(final List<FlightInformationRegion> list, final String identifier, final Function<FlightInformationRegion, ReadOnlyStringProperty> queryExtractor) {
        final List<FlightInformationRegion> byPrefixPosition = list
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
        private final List<FlightInformationRegion> result;
        private final boolean noResult;
        private final boolean oneResult;
        private final boolean manyResults;

        private QueryResult(final List<FlightInformationRegion> result) {
            this.result = result;
            this.noResult = result.isEmpty();
            this.oneResult = result.size() == 1;
            this.manyResults = result.size() > 1;
        }
    }
}
