package net.marvk.fs.vatsim.map.data;

import javafx.scene.paint.Color;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
public class ColorScheme implements UniquelyIdentifiable {
    public static final int CURRENT_VERSION = 1;
    int version;
    UUID uuid;
    String name;
    Map<String, Color> colorMap;
    Map<String, Boolean> toggleMap;

    ColorScheme(final UUID uuid, final int version, final String name, final Map<String, Color> colorMap, final Map<String, Boolean> toggleMap) {
        this.uuid = uuid;
        this.version = version;
        this.name = name;
        this.colorMap = Map.copyOf(colorMap);
        this.toggleMap = toggleMap;
    }

    public ColorScheme(final String name, final Map<String, Color> colorMap, final Map<String, Boolean> toggleMap) {
        this(
                UUID.randomUUID(),
                CURRENT_VERSION,
                name,
                colorMap,
                toggleMap
        );
    }

    public boolean isOutdated() {
        return version != CURRENT_VERSION;
    }
}
