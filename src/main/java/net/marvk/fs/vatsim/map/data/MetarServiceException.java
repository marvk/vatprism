package net.marvk.fs.vatsim.map.data;

import java.io.Serial;

public class MetarServiceException extends Exception {
    @Serial
    private static final long serialVersionUID = -3180186900893136820L;

    public MetarServiceException(final String icao) {
        super("Failed to fetch metar for icao \"%s\"".formatted(icao));
    }

    public MetarServiceException(final String icao, final Throwable cause) {
        super("Failed to fetch metar for icao \"%s\"".formatted(icao), cause);
    }
}
