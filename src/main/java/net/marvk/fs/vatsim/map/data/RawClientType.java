package net.marvk.fs.vatsim.map.data;

import java.util.Locale;

public enum RawClientType {
    PILOT, ATC, UNKNOWN;

    public static RawClientType fromString(final String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "pilot" -> PILOT;
            case "atc" -> ATC;
            default -> UNKNOWN;
        };
    }
}
