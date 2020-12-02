package net.marvk.fs.vatsim.map.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.marvk.fs.vatsim.api.ExampleDataSource;
import net.marvk.fs.vatsim.api.SimpleVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiDataSource;
import net.marvk.fs.vatsim.map.data.*;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(VatsimApiDataSource.class).to(ExampleDataSource.class).in(Singleton.class);
        bind(AirportRepository.class).in(Singleton.class);
        bind(ClientRepository.class).in(Singleton.class);
        bind(FlightInformationRegionRepository.class).in(Singleton.class);
        bind(FlightInformationRegionBoundaryRepository.class).in(Singleton.class);
        bind(UpperInformationRegionRepository.class).in(Singleton.class);
        bind(InternationalDateLineRepository.class).in(Singleton.class);

        bind(Airport.class);
        bind(Client.class);
        bind(FlightInformationRegionBoundary.class);
        bind(FlightInformationRegion.class);
        bind(UpperInformationRegion.class);
    }

    @Provides
    @Singleton
    public VatsimApi vatsimApi(final VatsimApiDataSource dataSource) {
        return new SimpleVatsimApi(dataSource);
    }

    @Provides
    @Named("shapefileUrl")
    public URL shapefileUrl() {
        return getClass().getResource("/net/marvk/fs/vatsim/map/ne_50m_land/ne_50m_land.shp");
    }

    @Provides
    @Singleton
    @Named("world")
    public List<Polygon> worldShape(@Named("shapefileUrl") final URL shapefileUrl) throws IOException {
        final ShapefileReader shapefileReader = new ShapefileReader(
                new ShpFiles(shapefileUrl),
                false,
                false,
                new GeometryFactory()
        );

        try {
            final List<Polygon> result = new ArrayList<>();

            while (shapefileReader.hasNext()) {
                final ShapefileReader.Record record = shapefileReader.nextRecord();

                final Object shape = record.shape();

                if (shape instanceof MultiLineString) {
                    final MultiLineString mls = (MultiLineString) shape;

                    for (int i = 0; i < mls.getNumGeometries(); i++) {
                        result.add(new Polygon(mls.getGeometryN(i)));
                    }
                } else if (shape instanceof MultiPolygon) {
                    final MultiPolygon mp = (MultiPolygon) shape;

                    for (int i = 0; i < mp.getNumGeometries(); i++) {
                        result.add(new Polygon(mp.getGeometryN(i)));
                    }
                }
            }

            return Collections.unmodifiableList(result);
        } finally {
            shapefileReader.close();
        }
    }
}
