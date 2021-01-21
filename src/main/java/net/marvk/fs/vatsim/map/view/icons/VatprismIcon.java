package net.marvk.fs.vatsim.map.view.icons;

import org.kordamp.ikonli.Ikon;

public enum VatprismIcon implements Ikon {
    FIR("vatprism-fir", '\u0061'),
    FIR_INACTIVE("vatprism-fir-inactive", '\u0064'),
    UIR("vatprism-uir", '\u0063'),
    UIR_INACTIVE("vatprism-uir-inactive", '\uE800'),
    AIRPORT("vatprism-airport", '\u0065'),
    AIRPORT_INACTIVE("vatprism-airport-inactive", '\u0066'),
    AIRPORT_LABEL("vatprism-airport-label", '\u0067'),
    PILOT("vatprism-pilot", '\u0068'),
    PILOT_LABEL("vatprism-pilot-label", '\u0069'),
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
