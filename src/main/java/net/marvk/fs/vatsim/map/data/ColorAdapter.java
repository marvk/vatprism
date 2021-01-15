package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;

class ColorAdapter implements Adapter<Color> {
    @Override
    public Color deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        return Color.valueOf(json.getAsString());
    }

    @Override
    public JsonElement serialize(final Color src, final Type typeOfSrc, final JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
