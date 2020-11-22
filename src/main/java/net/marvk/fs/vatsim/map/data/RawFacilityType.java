package net.marvk.fs.vatsim.map.data;

import java.util.Arrays;
import java.util.List;

public enum RawFacilityType {
    NONE(null, "OBS"),
    FLIGHT_SERVICE_STATION("FSS"),
    DELIVERY("DEL"),
    GROUND("GND"),
    TOWER_OR_ATIS("TWR", "ATIS"),
    APPROACH("APP"),
    CENTER("CTR");

    private final List<String> suffixes;

    RawFacilityType(final String... suffixes) {
        this.suffixes = Arrays.asList(suffixes);
    }

    public static RawFacilityType fromString(final String facilityType) {
        return switch (facilityType) {
            case "1" -> FLIGHT_SERVICE_STATION;
            case "2" -> DELIVERY;
            case "3" -> GROUND;
            case "4" -> TOWER_OR_ATIS;
            case "5" -> APPROACH;
            case "6" -> CENTER;
            default -> NONE;
        };
    }
}
