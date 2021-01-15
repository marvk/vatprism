package net.marvk.fs.vatsim.map.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class JsonSerializationUtil {
    private JsonSerializationUtil() {
        throw new AssertionError("No instances of utility class " + JsonSerializationUtil.class);
    }

    public static <T> List<T> deserializeToList(final JsonElement jsonElement, final JsonDeserializationContext context, final Class<T> clazz) {
        return StreamSupport.stream(jsonElement.getAsJsonArray().spliterator(), false)
                            .map(e -> deserializeToClass(e, context, clazz))
                            .collect(Collectors.toList());
    }

    public static <T> T deserializeToClass(final JsonElement jsonElement, final JsonDeserializationContext context, final Class<T> clazz) {
        return context.deserialize(jsonElement, TypeToken.get(clazz).getType());
    }
}
