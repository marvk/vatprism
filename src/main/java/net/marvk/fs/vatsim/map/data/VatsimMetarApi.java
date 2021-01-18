package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;

import java.util.Optional;

public class VatsimMetarApi implements MetarApi {
    private final VatsimApi vatsimApi;
    private final Deserializer<Metar> vatsimMetarDeserializer;

    @Inject
    public VatsimMetarApi(final VatsimApi vatsimApi, @Named("vatsimApiMetarDeserializer") final MetarDeserializer metarDeserializer) {
        this.vatsimApi = vatsimApi;
        this.vatsimMetarDeserializer = metarDeserializer;
    }

    @Override
    public Optional<Metar> fetchMetar(final String icao) throws MetarApiException {
        final String metar = fetchMetarFromApi(icao);
        if (metar == null || metar.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(vatsimMetarDeserializer.deserialize(metar));
        } catch (final Exception e) {
            throw new MetarApiException(icao, e);
        }
    }

    private String fetchMetarFromApi(final String icao) throws MetarApiException {
        try {
            return vatsimApi.metar(icao).getMetar();
        } catch (final VatsimApiException e) {
            throw new MetarApiException(icao, e);
        }
    }
}
