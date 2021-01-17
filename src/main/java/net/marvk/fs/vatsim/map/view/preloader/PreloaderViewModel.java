package net.marvk.fs.vatsim.map.view.preloader;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.CompositeCommand;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.CachedVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.ReloadRepositoryCommand;
import net.marvk.fs.vatsim.map.view.main.MainView;
import net.marvk.fs.vatsim.map.view.main.MainViewModel;

@Log4j2
public class PreloaderViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<ViewTuple<MainView, MainViewModel>> viewTuple = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private final RepositoryLoader repositoryLoader;

    @Inject
    public PreloaderViewModel(final RepositoryLoader repositoryLoader) {
        this.repositoryLoader = repositoryLoader;
    }

    public void load() {
        Platform.runLater(() -> {
            repositoryLoader.load();
            viewTuple.set(FluentViewLoader.fxmlView(MainView.class).load());
        });
    }

    public ViewTuple<MainView, MainViewModel> getViewTuple() {
        return viewTuple.get();
    }

    public ReadOnlyObjectProperty<ViewTuple<MainView, MainViewModel>> viewTupleProperty() {
        return viewTuple.getReadOnlyProperty();
    }

    ReadOnlyObjectProperty<ViewTuple<MainView, MainViewModel>> viewTuplePropertyWritable() {
        return viewTuple;
    }

    public double getProgress() {
        return progress.get();
    }

    public ReadOnlyDoubleProperty progressProperty() {
        return progress.getReadOnlyProperty();
    }

    ReadOnlyDoubleWrapper progressPropertyWritable() {
        return progress;
    }

    private static class RepositoryLoader {
        private final RatingsLoader ratingsLoader;
        private final AirportRepository airportRepository;
        private final ClientRepository clientRepository;
        private final FlightInformationRegionRepository flightInformationRegionRepository;
        private final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository;
        private final UpperInformationRegionRepository upperInformationRegionRepository;
        private final InternationalDateLineRepository internationalDateLineRepository;
        private final CountryRepository countryRepository;
        private final VatsimApi vatsimApi;

        @Inject
        public RepositoryLoader(
                final RatingsLoader ratingsLoader,
                final AirportRepository airportRepository,
                final ClientRepository clientRepository,
                final FlightInformationRegionRepository flightInformationRegionRepository,
                final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
                final UpperInformationRegionRepository upperInformationRegionRepository,
                final InternationalDateLineRepository internationalDateLineRepository,
                final CountryRepository countryRepository,
                final VatsimApi vatsimApi
        ) {

            this.ratingsLoader = ratingsLoader;
            this.airportRepository = airportRepository;
            this.clientRepository = clientRepository;
            this.flightInformationRegionRepository = flightInformationRegionRepository;
            this.flightInformationRegionBoundaryRepository = flightInformationRegionBoundaryRepository;
            this.upperInformationRegionRepository = upperInformationRegionRepository;
            this.internationalDateLineRepository = internationalDateLineRepository;
            this.countryRepository = countryRepository;
            this.vatsimApi = vatsimApi;
        }

        public void load() {
            final var loadRatings = new DelegateCommand(() -> new Action() {
                @Override
                protected void action() throws Exception {
                    ratingsLoader.loadRatings();
                }
            });
            final var loadAirports = new ReloadRepositoryCommand(airportRepository);
            final var loadInternationalDateLine = new ReloadRepositoryCommand(internationalDateLineRepository);
            final var loadFirs = new ReloadRepositoryCommand(flightInformationRegionRepository);
            final var loadFirbs = new ReloadRepositoryCommand(flightInformationRegionBoundaryRepository);
            final var loadUirs = new ReloadRepositoryCommand(upperInformationRegionRepository);
            final var loadCountries = new ReloadRepositoryCommand(countryRepository);
            final var loadClients = new ReloadRepositoryCommand(clientRepository);

            final CompositeCommand compositeCommand = new CompositeCommand(
                    loadRatings,
                    loadInternationalDateLine,
                    loadCountries,
                    loadFirs,
                    loadFirbs,
                    loadUirs,
                    loadAirports,
                    loadClients,
                    new DelegateCommand(() -> new Action() {
                        @Override
                        protected void action() {
                            // Clear unneeded cached values
                            if (vatsimApi instanceof CachedVatsimApi) {
                                ((CachedVatsimApi) vatsimApi).clear();
                            }
                        }
                    })
            );

            log.info("Loading data");
            compositeCommand.execute();
        }
    }
}
