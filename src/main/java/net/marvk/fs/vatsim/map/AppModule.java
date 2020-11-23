package net.marvk.fs.vatsim.map;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.api.ExampleDataSource;
import net.marvk.fs.vatsim.api.HttpDataSource;
import net.marvk.fs.vatsim.api.SimpleVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.repository.AirportRepository;
import net.marvk.fs.vatsim.map.repository.ClientRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionBoundaryRepository;
import net.marvk.fs.vatsim.map.repository.FlightInformationRegionRepository;

public class AppModule extends AbstractModule {
    @SneakyThrows
    @Override
    protected void configure() {
        bind(VatsimApi.class).toInstance(new SimpleVatsimApi(new HttpDataSource()));
        bind(AirportRepository.class).in(Singleton.class);
        bind(ClientRepository.class).in(Singleton.class);
        bind(FlightInformationRegionRepository.class).in(Singleton.class);
        bind(FlightInformationRegionBoundaryRepository.class).in(Singleton.class);

        bind(AirportViewModel.class);
        bind(ClientStatusViewModel.class);
        bind(ClientViewModel.class);
        bind(ControllerDataViewModel.class);
        bind(FlightInformationRegionBoundaryViewModel.class);
        bind(FlightInformationRegionViewModel.class);
        bind(FlightPlanViewModel.class);
        bind(UpperInformationRegionViewModel.class);
    }
}
