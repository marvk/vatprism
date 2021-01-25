package net.marvk.fs.vatsim.map.data;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;

class ObservableValueJsonAdapter implements JsonAdapter<ObservableValue<?>> {
    public static final Type TYPE = new TypeToken<ObservableValue<?>>() {
    }.getType();

    @Override
    public ObservableValue<?> deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject o = json.getAsJsonObject();

        final ObservableType type = JsonSerializationUtil.deserializeToClass(o.get("type"), context, ObservableType.class);
        final JsonElement value = o.get("value");

        return switch (type) {
            case INTEGER -> new SimpleIntegerProperty(value.getAsInt());
            case DECIMAL -> new SimpleDoubleProperty(value.getAsDouble());
            case STRING -> new SimpleStringProperty(value.getAsString());
            case COLOR -> new SimpleObjectProperty<>(JsonSerializationUtil.deserializeToClass(value, context, Color.class));
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

    public enum ObservableType {
        INTEGER,
        DECIMAL,
        STRING,
        COLOR,
        BOOLEAN
    }
}
