package net.marvk.fs.vatsim.map.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PolygonRepository implements ReloadableRepository<Polygon> {
    private final List<String> names;
    private final List<URL> urls;
    private ObservableList<Polygon> polygons;

    public PolygonRepository(final List<String> names, final List<URL> urls) {
        if (names.size() != urls.size()) {
            throw new IllegalArgumentException();
        }

        this.names = names;
        this.urls = urls;
    }

    @Override
    public ObservableList<Polygon> list() {
        if (polygons == null) {
            throw new IllegalStateException("Repository has not been reloaded");
        }

        return polygons;
    }

    @Override
    public Polygon getByKey(final String key) {
        throw new UnsupportedOperationException();
    }

    private void loadPolygons() throws IOException {
        final Collection<List<Polygon>> result = new ArrayList<>();

        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            final URL url = urls.get(i);

            result.add(loadPolygons(name, url));
        }

        polygons = result
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(FXCollections::observableArrayList),
                        FXCollections::unmodifiableObservableList)
                );
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private static List<Polygon> loadPolygons(final String name, final URL url) throws IOException {
        final DataStore dataStore = DataStoreFinder.getDataStore(Map.of("url", url));
        final SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);

        try (SimpleFeatureIterator features = featureSource.getFeatures().features()) {
            final List<Polygon> result = new ArrayList<>();

            int id = 0;

            while (features.hasNext()) {
                final SimpleFeature feature = features.next();

                final Object shape = feature.getDefaultGeometry();

                if (shape instanceof MultiLineString) {
                    final MultiLineString mls = (MultiLineString) shape;

                    for (int i = 0; i < mls.getNumGeometries(); i++) {
                        result.add(new Polygon(mls.getGeometryN(i), "%s_%d".formatted(name, id)));
                        id += 1;
                    }
                } else if (shape instanceof MultiPolygon) {
                    final MultiPolygon mp = (MultiPolygon) shape;

                    for (int i = 0; i < mp.getNumGeometries(); i++) {
                        final Geometry geometryN = mp.getGeometryN(i);
                        final org.locationtech.jts.geom.Polygon polygon = (org.locationtech.jts.geom.Polygon) geometryN;

                        result.add(new Polygon(polygon, "%s_%d".formatted(name, id)));
                        id += 1;
                    }
                }
            }

            return result;
        }
    }

    @Override
    public void reload() throws RepositoryException {
        try {
            loadPolygons();
        } catch (final IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void reloadAsync(final Runnable onSucceed) throws RepositoryException {
        throw new UnsupportedOperationException();
    }
}
