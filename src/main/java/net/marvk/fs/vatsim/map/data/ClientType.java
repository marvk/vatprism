package net.marvk.fs.vatsim.map.data;

import java.util.Locale;

public enum ClientType {
    PILOT, ATC, UNKNOWN;

    public static ClientType fromString(final String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "pilot" -> PILOT;
            case "atc" -> ATC;
            default -> UNKNOWN;
        };
    }
}
