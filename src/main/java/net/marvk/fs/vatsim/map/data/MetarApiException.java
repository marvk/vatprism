package net.marvk.fs.vatsim.map.data;

import java.io.Serial;

public class MetarApiException extends Exception {
    @Serial
    private static final long serialVersionUID = -3400889594658976983L;

    public MetarApiException(final String icao) {
        super("Failed to fetch metar for icao \"%s\"".formatted(icao));
    }

    public MetarApiException(final String icao, final Throwable cause) {
        super("Failed to fetch metar for icao \"%s\"".formatted(icao), cause);
    }
}
