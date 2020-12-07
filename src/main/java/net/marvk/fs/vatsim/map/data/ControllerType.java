package net.marvk.fs.vatsim.map.data;

import java.util.Comparator;
import java.util.Locale;

public enum ControllerType {
    NONE, OBS, ATIS, DEL, GND, TWR, APP, DEP, CTR, FSS;

    public static ControllerType fromString(final String s) {
        return fromString(s, NONE);
    }

    public static ControllerType fromString(final String s, final ControllerType defaultControllerType) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "obs" -> OBS;
            case "atis" -> ATIS;
            case "del" -> DEL;
            case "gnd" -> GND;
            case "twr" -> TWR;
            case "app" -> APP;
            case "dep" -> DEP;
            case "ctr" -> CTR;
            case "fss" -> FSS;
            default -> defaultControllerType;
        };
    }

    // TODO
    public static final Comparator<ControllerType> COMPARATOR = Enum::compareTo;
}
