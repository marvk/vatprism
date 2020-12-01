package net.marvk.fs.vatsim.map.data;

import java.util.Locale;

public enum FlightType {
    IFR, VFR, DVFR, SVFR, UNKNOWN;

    public static FlightType fromString(final String identifier) {
        if (identifier == null) {
            return UNKNOWN;
        }

        return switch (identifier.toLowerCase(Locale.ROOT)) {
            case "i" -> IFR;
            case "v" -> VFR;
            case "d" -> DVFR;
            case "s" -> SVFR;
            default -> UNKNOWN;
        };
    }
}
