package net.marvk.fs.vatsim.map.data;

import java.util.Optional;

public interface MetarApi {
    default Optional<Metar> fetchMetar(final Airport airport) throws MetarApiException {
        return fetchMetar(airport.getIcao());
    }

    Optional<Metar> fetchMetar(final String icao) throws MetarApiException;
}
