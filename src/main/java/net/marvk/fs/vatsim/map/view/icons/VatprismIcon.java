package net.marvk.fs.vatsim.map.view.icons;

import org.kordamp.ikonli.Ikon;

public enum VatprismIcon implements Ikon {
    FIR("vatprism-fir", 'a'),
    FIR_INACTIVE("vatprism-fir-inactive", 'b'),
    UIR("vatprism-uir", 'c'),
    UIR_INACTIVE("vatprism-uir-inactive", 'd'),
    AIRPORT("vatprism-airport", 'e'),
    AIRPORT_INACTIVE("vatprism-airport-inactive", 'f'),
    AIRPORT_LABEL("vatprism-airport-label", 'g');

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
