package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.Optional;

public class SimpleMetarService implements MetarService {
    private final ObservableMap<String, Metar> lastMetars = FXCollections.observableHashMap();
    private final ObservableMap<String, Metar> lastMetarsUnmodifiable = FXCollections.unmodifiableObservableMap(lastMetars);

    private final MetarApi metarApi;

    @Inject
    public SimpleMetarService(final MetarApi metarApi) {
        this.metarApi = metarApi;
    }

    @Override
    public Optional<Metar> latestMetar(final String icao) throws MetarServiceException {
        final Optional<Metar> metar = tryFetchMetar(icao);
        metar.ifPresent(e -> lastMetars.put(icao, e));
        return metar;
    }

    private Optional<Metar> tryFetchMetar(final String icao) throws MetarServiceException {
        try {
            return metarApi.fetchMetar(icao);
        } catch (final MetarApiException e) {
            throw new MetarServiceException(icao, e);
        }
    }

    @Override
    public Optional<Metar> lastMetar(final String icao) {
        return Optional.ofNullable(lastMetars.get(icao));
    }

    @Override
    public ObservableMap<String, Metar> lastMetars() {
        return lastMetarsUnmodifiable;
    }
}
