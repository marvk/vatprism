package net.marvk.fs.vatsim.map.data;

import javafx.collections.ObservableMap;

import java.util.Optional;

public interface MetarService {
    /**
     * @see MetarService#latestMetar(String)
     */
    default Optional<Metar> latestMetar(final Airport airport) throws MetarServiceException {
        return latestMetar(airport.getIcao());
    }

    /**
     * Fetch the latest metar at a specific airport if available
     *
     * @param icao the airport icao
     *
     * @return the latest metar at the specified airport if available
     *
     * @throws MetarServiceException if an error occurred while fetching the Metar
     */
    Optional<Metar> latestMetar(final String icao) throws MetarServiceException;

    /**
     * @see MetarService#lastMetar(String)
     */
    default Optional<Metar> lastMetar(final Airport airport) {
        return lastMetar(airport.getIcao());
    }

    /**
     * Get the last fetched metar at a specific airport if available
     *
     * @param icao the airport icao
     *
     * @return the last metar at the specified airport if available
     */
    Optional<Metar> lastMetar(final String icao);

    /**
     * @see MetarService#lastMetarIfAvailableElseLatest(String)
     */
    default Optional<Metar> lastMetarIfAvailableElseLatest(final Airport airport) throws MetarServiceException {
        return lastMetarIfAvailableElseLatest(airport.getIcao());
    }

    /**
     * Get the last fetched metar at a specific airport if available, else fetch the latest metar if available
     *
     * @param icao the airport icao
     *
     * @return the last fetched metar at a specific airport if available, else the latest metar if available
     *
     * @throws MetarServiceException if an error occurred while fetching the Metar
     */
    default Optional<Metar> lastMetarIfAvailableElseLatest(final String icao) throws MetarServiceException {
        final Optional<Metar> metar = lastMetar(icao);

        if (metar.isPresent()) {
            return metar;
        }

        return latestMetar(icao);
    }

    /**
     * Returns an unmodifiable view of a ObservableMap of the last metars
     *
     * @return a map of the last metars, with icao as key and metar as value
     */
    ObservableMap<String, Metar> lastMetars();
}
