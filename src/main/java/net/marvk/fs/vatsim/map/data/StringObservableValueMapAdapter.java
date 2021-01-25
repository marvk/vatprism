package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.value.ObservableValue;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class StringObservableValueMapAdapter implements JsonAdapter<Map<String, ObservableValue<?>>> {
    public static final Type TYPE = new TypeToken<Map<String, ObservableValue<?>>>() {
    }.getType();

    @Override
    public Map<String, ObservableValue<?>> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final Map<String, ObservableValue<?>> result = new HashMap<>();
        add("", json.getAsJsonObject(), result, context);
        return result;
    }

    @Override
    public JsonElement serialize(final Map<String, ObservableValue<?>> src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();

        src.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> {
            final String[] keys = e.getKey().split("\\.");

            JsonObject current = result;

            for (int i = 0; i < keys.length - 1; i++) {
                final String key = keys[i];
                final JsonElement maybeNext = current.get(key);
                final JsonObject next;
                if (maybeNext == null) {
                    final JsonObject value = new JsonObject();
                    current.add(key, value);
                    next = value;
                } else {
                    next = maybeNext.getAsJsonObject();
                }
                current = next;
            }

            final JsonElement value = context.serialize(e.getValue(), ObservableValueJsonAdapter.TYPE);
            current.add(keys[keys.length - 1], value);
        });

        return result;
    }

    private static void add(final String key, final JsonObject jsonObject, final Map<String, ObservableValue<?>> result, final JsonDeserializationContext context) {
        if (jsonObject.has("type") && jsonObject.has("value")) {
            result.put(key, context.deserialize(jsonObject, ObservableValueJsonAdapter.TYPE));
        } else {
            for (final Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                final String newKey = key.isBlank() ? e.getKey() : "%s.%s".formatted(key, e.getKey());
                add(newKey, e.getValue().getAsJsonObject(), result, context);
            }
        }
    }
}
