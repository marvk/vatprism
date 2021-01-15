package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static net.marvk.fs.vatsim.map.data.JsonSerializationUtil.deserializeToClass;

public class JsonObservablePreferencesSerializer implements Serializer<Map<String, ObservableValue<?>>> {
    private static final Type MAP_TYPE = new TypeToken<Map<String, ObservableValue<?>>>() {
    }.getType();

    private static final Type OBSERVABLE_TYPE = new TypeToken<ObservableValue<?>>() {
    }.getType();

    private final Gson gson;

    public JsonObservablePreferencesSerializer() {
        gson = new GsonBuilder()
                .registerTypeAdapter(MAP_TYPE, new MapAdapter())
                .registerTypeAdapter(OBSERVABLE_TYPE, new ObservableValueAdapter())
                .registerTypeAdapter(Color.class, new ColorAdapter())
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    @Override
    public String serialize(final Map<String, ObservableValue<?>> map) {
        return gson.toJson(map, MAP_TYPE);
    }

    @Override
    public Map<String, ObservableValue<?>> deserialize(final String s) {
        return gson.fromJson(s, MAP_TYPE);
    }

    private static class MapAdapter implements Adapter<Map<String, ObservableValue<?>>> {
        @Override
        public Map<String, ObservableValue<?>> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final Map<String, ObservableValue<?>> result = new HashMap<>();
            add("", json.getAsJsonObject(), result, context);
            return result;
        }

        private void add(final String key, final JsonObject jsonObject, final Map<String, ObservableValue<?>> result, final JsonDeserializationContext context) {
            if (jsonObject.has("type") && jsonObject.has("value")) {
                result.put(key, context.deserialize(jsonObject, OBSERVABLE_TYPE));
            } else {
                for (final Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    final String newKey = key.isBlank() ? e.getKey() : "%s.%s".formatted(key, e.getKey());
                    add(newKey, e.getValue().getAsJsonObject(), result, context);
                }
            }
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

                final JsonElement value = context.serialize(e.getValue(), OBSERVABLE_TYPE);
                current.add(keys[keys.length - 1], value);
            });

            return result;
        }
    }

    private static class ObservableValueAdapter implements Adapter<ObservableValue<?>> {
        @Override
        public ObservableValue<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            final JsonObject o = json.getAsJsonObject();

            final ObservableType type = deserializeToClass(o.get("type"), context, ObservableType.class);
            final JsonElement value = o.get("value");

            return switch (type) {
                case INTEGER -> new SimpleIntegerProperty(value.getAsInt());
                case DECIMAL -> new SimpleDoubleProperty(value.getAsDouble());
                case STRING -> new SimpleStringProperty(value.getAsString());
                case COLOR -> new SimpleObjectProperty<>(deserializeToClass(value, context, Color.class));
                case BOOLEAN -> new SimpleBooleanProperty(value.getAsBoolean());
            };
        }

        @Override
        public JsonElement serialize(final ObservableValue<?> src, final Type typeOfSrc, final JsonSerializationContext context) {
            final JsonObject result = new JsonObject();
//
            result.add("type", context.serialize(type(src)));
            result.add("value", context.serialize(src.getValue()));

            return result;
        }

        private static ObservableType type(final ObservableValue<?> observableValue) {
            if (observableValue instanceof ObservableStringValue) {
                return ObservableType.STRING;
            } else if (observableValue instanceof ObservableObjectValue) {
                // assume COLOR here
                return ObservableType.COLOR;
            } else if (observableValue instanceof ObservableIntegerValue) {
                return ObservableType.INTEGER;
            } else if (observableValue instanceof ObservableDoubleValue) {
                return ObservableType.DECIMAL;
            } else if (observableValue instanceof ReadOnlyBooleanProperty) {
                return ObservableType.BOOLEAN;
            }
            throw new IllegalArgumentException("Unable to coerce type from observable value %s".formatted(observableValue));
        }
    }

    private enum ObservableType {
        INTEGER,
        DECIMAL,
        STRING,
        COLOR,
        BOOLEAN
    }
}
