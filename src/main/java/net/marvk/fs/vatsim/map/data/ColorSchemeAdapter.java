package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class ColorSchemeAdapter implements Adapter<ColorScheme> {
    private static final Type MAP_TYPE = new TypeToken<Map<String, Color>>() {
    }.getType();

    private final Gson gson;

    public ColorSchemeAdapter() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ObservableValueJsonAdapter.TYPE, new ObservableValueJsonAdapter())
                .registerTypeAdapter(Color.class, new ColorJsonAdapter())
                .registerTypeAdapter(ColorScheme.class, new ColorSchemeJsonAdapter())
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    @Override
    public ColorScheme deserialize(final String s) {
        return gson.fromJson(s, ColorScheme.class);
    }

    @Override
    public String serialize(final ColorScheme colorScheme) {
        return gson.toJson(colorScheme);
    }

    private static class ColorSchemeJsonAdapter implements JsonAdapter<ColorScheme> {
        @Override
        public ColorScheme deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject o = json.getAsJsonObject();
            final JsonElement uuid = o.get("uuid");
            final JsonElement name = o.get("name");
            final JsonElement values = o.get("values");

            return new ColorScheme(
                    UUID.fromString(uuid.getAsString()),
                    name.getAsString(),
                    context.deserialize(values, MAP_TYPE)
            );
        }

        @Override
        public JsonElement serialize(final ColorScheme src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();

            result.addProperty("uuid", src.getUuid().toString());
            result.addProperty("name", src.getName());
            result.add("values", context.serialize(src.getColorMap(), MAP_TYPE));

            return result;
        }
    }
}
