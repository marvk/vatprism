package net.marvk.fs.vatsim.map.view.preloader;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.CachedVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.main.MainView;
import net.marvk.fs.vatsim.map.view.main.MainViewModel;

import java.util.List;

@Log4j2
public class PreloaderViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<ViewTuple<MainView, MainViewModel>> viewTuple = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper taskDescription = new ReadOnlyStringWrapper();

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private final RepositoryLoader repositoryLoader;

    @Inject
    public PreloaderViewModel(final RepositoryLoader repositoryLoader) {
        this.repositoryLoader = repositoryLoader;
    }

    public void load() {
        repositoryLoader.progressProperty()
                        .addListener((observable, oldValue, newValue) -> progress.set(newValue.doubleValue()));
        repositoryLoader.currentTaskDescriptionProperty()
                        .addListener((observable, oldValue, newValue) -> taskDescription.set(newValue));

        repositoryLoader.start();
        repositoryLoader.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                taskDescription.set("Loading View");
            });
            Platform.runLater(() -> {
                viewTuple.set(FluentViewLoader.fxmlView(MainView.class).load());
                progress.set(1);
            });
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

    public String getTaskDescription() {
        return taskDescription.get();
    }

    public ReadOnlyStringProperty taskDescriptionProperty() {
        return taskDescription.getReadOnlyProperty();
    }

    private static class RepositoriesService extends Service<Void> {
        @Override
        protected Task<Void> createTask() {
            return new Task<>() {
                @Override
                protected Void call() throws Exception {
                    return null;
                }
            };
        }
    }

    private static class CallableTask extends Task<Void> {
        private final String taskName;
        private final VoidCallable callable;

        public CallableTask(final String taskName, final VoidCallable callable) {
            this.taskName = taskName;
            this.callable = callable;
        }

        @Override
        protected Void call() throws Exception {
            callable.call();
            updateProgress(1, 1);
            return null;
        }
    }

    private static class CompositeTask extends Task<Void> {
        private final ReadOnlyStringWrapper currentTaskDescription = new ReadOnlyStringWrapper();
        private final List<CallableTask> tasks;

        public CompositeTask(final List<CallableTask> tasks) {
            this.tasks = tasks;
        }

        @Override
        protected Void call() throws Exception {
            final int n = tasks.size();
            for (int i = 0; i < n; i++) {
                final CallableTask task = tasks.get(i);
                Platform.runLater(() -> currentTaskDescription.set(task.taskName));

                task.call();

                final int progressDone = i + 1;
                Platform.runLater(() -> {
                    updateProgress(progressDone, n);
                });
            }

            return null;
        }

        public String getCurrentTaskDescription() {
            return currentTaskDescription.get();
        }

        public ReadOnlyStringProperty currentTaskDescriptionProperty() {
            return currentTaskDescription.getReadOnlyProperty();
        }
    }

    private static class RepositoryTask extends CallableTask {
        public RepositoryTask(final String taskName, final ReloadableRepository<?> repository) {
            super(taskName, repository::reload);
        }
    }

    private static class RepositoryLoader extends Service<Void> {
        private final ReadOnlyStringWrapper currentTaskDescription = new ReadOnlyStringWrapper();

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

        @Override
        protected Task<Void> createTask() {
            final var loadRatings = new CallableTask("Loading ratings", ratingsLoader::loadRatings);
            final var loadAirports = new RepositoryTask("Loading Airports", airportRepository);
            final var loadInternationalDateLine = new RepositoryTask("Loading International Date Line", internationalDateLineRepository);
            final var loadFirs = new RepositoryTask("Loading Flight Information Regions", flightInformationRegionRepository);
            final var loadFirbs = new RepositoryTask("Loading Flight Information Region Boundaries", flightInformationRegionBoundaryRepository);
            final var loadUirs = new RepositoryTask("Loading Upper Information Regions", upperInformationRegionRepository);
            final var loadCountries = new RepositoryTask("Loading Countries", countryRepository);
            final var loadClients = new RepositoryTask("Loading Clients", clientRepository);
            final var clearCaches = new CallableTask("Cleaning Caches", () -> {
                if (vatsimApi instanceof CachedVatsimApi) {
                    ((CachedVatsimApi) vatsimApi).clear();
                }
            });

            final List<CallableTask> tasks = List.of(
                    loadRatings,
                    loadAirports,
                    loadInternationalDateLine,
                    loadFirs,
                    loadFirbs,
                    loadUirs,
                    loadCountries,
                    loadClients,
                    clearCaches
            );

            final CompositeTask compositeTask = new CompositeTask(tasks);
            currentTaskDescription.bind(compositeTask.currentTaskDescriptionProperty());
            return compositeTask;
        }

        public String getCurrentTaskDescription() {
            return currentTaskDescription.get();
        }

        public ReadOnlyStringProperty currentTaskDescriptionProperty() {
            return currentTaskDescription.getReadOnlyProperty();
        }
    }

    @FunctionalInterface
    private interface VoidCallable {
        void call() throws Exception;
    }
}
