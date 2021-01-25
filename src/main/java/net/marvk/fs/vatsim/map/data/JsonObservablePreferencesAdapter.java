package net.marvk.fs.vatsim.map.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.util.Map;

public class JsonObservablePreferencesAdapter implements Adapter<Map<String, ObservableValue<?>>> {
    private final Gson gson;

    public JsonObservablePreferencesAdapter() {
        gson = new GsonBuilder()
                .registerTypeAdapter(StringObservableValueMapAdapter.TYPE, new StringObservableValueMapAdapter())
                .registerTypeAdapter(ObservableValueJsonAdapter.TYPE, new ObservableValueJsonAdapter())
                .registerTypeAdapter(Color.class, new ColorJsonAdapter())
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }

    @Override
    public String serialize(final Map<String, ObservableValue<?>> map) {
        return gson.toJson(map, StringObservableValueMapAdapter.TYPE);
    }

    @Override
    public Map<String, ObservableValue<?>> deserialize(final String s) {
        return gson.fromJson(s, StringObservableValueMapAdapter.TYPE);
    }
}
