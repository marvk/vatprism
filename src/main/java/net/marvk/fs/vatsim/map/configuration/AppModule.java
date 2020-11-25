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
import net.marvk.fs.vatsim.map.repository.*;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;

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

        bind(AirportViewModel.class);
        bind(ClientStatusViewModel.class);
        bind(ClientViewModel.class);
        bind(ControllerDataViewModel.class);
        bind(FlightInformationRegionBoundaryViewModel.class);
        bind(FlightInformationRegionViewModel.class);
        bind(FlightPlanViewModel.class);
        bind(UpperInformationRegionViewModel.class);
    }

    @Provides
    @Singleton
    public VatsimApi vatsimApi(final VatsimApiDataSource dataSource) {
        return new SimpleVatsimApi(dataSource);
    }

    @Provides
    @Named("shapefileUrl")
    public URL shapefileUrl() {
        return getClass().getResource("../ne_110m_coastline/ne_110m_coastline.shp");
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
                final MultiLineString mls = (MultiLineString) record.shape();
                result.add(new Polygon(mls));
            }

            return Collections.unmodifiableList(result);
        } finally {
            shapefileReader.close();
        }
    }
}
