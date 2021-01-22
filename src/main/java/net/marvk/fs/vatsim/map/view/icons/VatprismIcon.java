package net.marvk.fs.vatsim.map.view.icons;

import org.kordamp.ikonli.Ikon;

public enum VatprismIcon implements Ikon {
    FIR("vatprism-fir", '\ue800'),
    FIR_INACTIVE("vatprism-fir-inactive", '\ue808'),
    UIR("vatprism-uir", '\ue806'),
    UIR_INACTIVE("vatprism-uir-inactive", '\ue807'),
    AIRPORT("vatprism-airport", '\ue805'),
    AIRPORT_INACTIVE("vatprism-airport-inactive", '\ue802'),
    AIRPORT_LABEL("vatprism-airport-label", '\ue801'),
    PILOT("vatprism-pilot", '\ue803'),
    PILOT_LABEL("vatprism-pilot-label", '\ue804'),
    PILOT_ON_GROUND("vatprism-pilot-on-ground", '\ue80A'),
    DISCORD("vatprism-discord", '\ue809'),
    ;

    public static VatprismIcon findByDescription(final String description) {
        for (final VatprismIcon font : values()) {
            if (font.description.equals(description)) {
                return font;
            }
        }
        throw new IllegalArgumentException("Icon description '" + description + "' is invalid!");
    }

    private final String description;
    private final int code;

    VatprismIcon(final String description, final int code) {
        this.description = description;
        this.code = code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getCode() {
        return code;
    }
}
