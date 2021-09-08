package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ColorSchemeAdapter implements Adapter<ColorScheme> {
    private static final Type COLOR_MAP_TYPE = new TypeToken<Map<String, Color>>() {
    }.getType();
    private static final Type TOGGLES_MAP_TYPE = new TypeToken<Map<String, Boolean>>() {
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
            final JsonElement version = o.get("version");
            final JsonElement name = o.get("name");
            final JsonElement values = o.get("values");
            final JsonElement toggles = o.get("toggles");

            return new ColorScheme(
                    UUID.fromString(uuid.getAsString()),
                    version == null || version.isJsonNull() ? 0 : version.getAsInt(),
                    name.getAsString(),
                    context.deserialize(values, COLOR_MAP_TYPE),
                    deserializeTogglesMap(context, toggles)
            );
        }

        private static Map<String, Boolean> deserializeTogglesMap(final JsonDeserializationContext context, final JsonElement toggles) {
            final Map<String, Boolean> maybeToggles = context.deserialize(toggles, TOGGLES_MAP_TYPE);

            if (maybeToggles == null || maybeToggles.isEmpty()) {
                return new HashMap<>(Map.of(
                        "ui.auto_color", true,
                        "ui.auto_shade", true,
                        "ui.invert_background_shading", false,
                        "ui.invert_text_shading", false
                ));
            } else {
                return maybeToggles;
            }
        }

        @Override
        public JsonElement serialize(final ColorScheme src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();

            final Map<String, Color> colors = sortByKey(src.getColorMap());
            final Map<String, Boolean> toggles = sortByKey(src.getToggleMap());

            result.addProperty("uuid", src.getUuid().toString());
            result.addProperty("version", src.getVersion());
            result.addProperty("name", src.getName());
            result.add("values", context.serialize(colors, COLOR_MAP_TYPE));
            result.add("toggles", context.serialize(toggles, TOGGLES_MAP_TYPE));

            return result;
        }

        private static <T> LinkedHashMap<String, T> sortByKey(final Map<String, T> map) {
            return map
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (a, b) -> {
                                throw new AssertionError();
                            },
                            LinkedHashMap::new
                    ));
        }
    }
}
