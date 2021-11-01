package net.marvk.fs.vatsim.map.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.Feature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FeatureSetDeserializer implements Deserializer<FeatureSet> {
    private final Gson gson;
    private final FeatureJSON featureJson;

    public FeatureSetDeserializer() {
        gson = new Gson();
        featureJson = new FeatureJSON();
    }

    @SneakyThrows
    @Override
    public FeatureSet deserialize(final String s) {

        final SimpleFeatureCollection featureCollection = featureJson.readFeatureCollection(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));

        final List<Feature> extracted = featuresAsList(featureCollection);

        final String featureSetName = gson.fromJson(s, JsonObject.class).get("name").getAsString();

        final ArrayList<PointFeature> points = new ArrayList<>();
        final ArrayList<LineStringFeature> lineStrings = new ArrayList<>();

        for (final Feature feature : extracted) {
            final String name = (String) feature.getProperty("name").getValue();
            ;
        }

        return new FeatureSet(featureSetName, points, lineStrings);
    }

    private List<Feature> featuresAsList(final SimpleFeatureCollection read) {
        try (final SimpleFeatureIterator features = read.features()) {
            final List<Feature> result = new ArrayList<>();

            while (features.hasNext()) {
                result.add(features.next());
            }

            return result;
        }
    }

    public static void main(String[] args) throws IOException {
        final FeatureSetDeserializer featureSetDeserializer = new FeatureSetDeserializer();

        final String s = Files.readString(Paths.get("src/main/resources/net/marvk/fs/vatsim/map/addons/ctp-e21.geojson"));

        System.out.println(s);
        featureSetDeserializer.deserialize(s);
    }
}
