package net.marvk.fs.vatsim.map.data;

import java.util.Locale;

public enum RawFlightType {
    IFR, VFR, DVFR, SVFR, UNKNOWN;

    public static RawFlightType fromString(final String identifier) {
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
