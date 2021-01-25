package net.marvk.fs.vatsim.map.data;

import javafx.scene.paint.Color;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
public class ColorScheme implements UniquelyIdentifiable {
    UUID uuid;
    String name;
    Map<String, Color> colorMap;

    ColorScheme(final UUID uuid, final String name, final Map<String, Color> colorMap) {
        this.uuid = uuid;
        this.name = name;
        this.colorMap = Map.copyOf(colorMap);
    }

    public ColorScheme(final String name, final Map<String, Color> colorMap) {
        this(
                UUID.randomUUID(),
                name,
                colorMap
        );
    }
}
