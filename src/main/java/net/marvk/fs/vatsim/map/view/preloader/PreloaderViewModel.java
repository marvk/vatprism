package net.marvk.fs.vatsim.map.view.preloader;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.CachedVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.version.*;
import net.marvk.fs.vatsim.map.view.main.MainView;
import net.marvk.fs.vatsim.map.view.main.MainViewModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Log4j2
public class PreloaderViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<ViewTuple<MainView, MainViewModel>> viewTuple = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper taskDescription = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper error = new ReadOnlyStringWrapper();

    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
    private final ReadOnlyObjectWrapper<VersionResponse> versionResponse = new ReadOnlyObjectWrapper<>();
    private final Preferences preferences;
    private final VersionApi versionApi;
    private final HostServices hostServices;
    private final RepositoryLoader repositoryLoader;
    private final VersionProvider versionProvider;

    private final ObjectProperty<Throwable> exception = new SimpleObjectProperty<>();

    @Inject
    public PreloaderViewModel(final Preferences preferences, final VersionApi versionApi, final HostServices hostServices, final RepositoryLoader repositoryLoader, final VersionProvider versionProvider) {
        this.preferences = preferences;
        this.versionApi = versionApi;
        this.hostServices = hostServices;
        this.repositoryLoader = repositoryLoader;
        this.versionProvider = versionProvider;
    }

    public void load() {
        repositoryLoader.progressProperty()
                        .addListener((observable, oldValue, newValue) -> progress.set(newValue.doubleValue()));
        repositoryLoader.currentTaskDescriptionProperty()
                        .addListener((observable, oldValue, newValue) -> taskDescription.set(newValue));

        tryCheckVersion();

        repositoryLoader.setOnSucceeded(e -> {
            log.debug("Loading view");
            Platform.runLater(() -> taskDescription.set("Loading View"));
            Platform.runLater(() -> {
                try {
                    runTimed(() -> {
                        final var viewTuple = FluentViewLoader.fxmlView(MainView.class).load();
                        this.viewTuple.set(viewTuple);
                    }, "Loading view", "Loaded view");
                    progress.set(1);
                } catch (final Exception ex) {
                    failed(ex);
                }
            });
        });
        repositoryLoader.setOnFailed(e -> failed(repositoryLoader.getException()));

        repositoryLoader.start();
    }

    private void tryCheckVersion() {
        repositoryLoader.currentTaskDescription.set("Checking Version");

        final UpdateChannel channel =
                preferences.booleanProperty("general.prereleases").get()
                        ? UpdateChannel.EXPERIMENTAL
                        : UpdateChannel.STABLE;
        try {
            final VersionResponse versionResponse = versionApi.checkVersion(channel);
            if (versionResponse.getResult() == VersionResponse.Result.OUTDATED) {
                log.warn("Found newer version: %s".formatted(versionResponse.getLatestVersion()));
            } else {
                log.info("Version is current");
            }
            repositoryLoader.currentTaskDescription.set("Checked Version");
            this.versionResponse.set(versionResponse);
        } catch (final VersionApiException e) {
            log.error("Failed to fetch version", e);
        }
    }

    private void failed(final Throwable e) {
        exception.set(e);
        log.error("Failed preloader task \"%s\"".formatted(getTaskDescription()), e);
        error.set("Failed " + getTaskDescription().toLowerCase(Locale.ROOT));
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

    public VersionResponse getVersionResponse() {
        return versionResponse.get();
    }

    public ReadOnlyObjectProperty<VersionResponse> versionResponseProperty() {
        return versionResponse.getReadOnlyProperty();
    }

    public String getTaskDescription() {
        return taskDescription.get();
    }

    public ReadOnlyStringProperty taskDescriptionProperty() {
        return taskDescription.getReadOnlyProperty();
    }

    public String getError() {
        return error.get();
    }

    public ReadOnlyStringProperty errorProperty() {
        return error.getReadOnlyProperty();
    }

    public void goToIssuePage() {
        final String escapedError = error.get().replaceAll(" ", "+");
        hostServices.showDocument("https://github.com/marvk/vatprism/issues/new?assignees=&labels=bug&template=bug_report.md&title=Error+during+startup%3A+" + escapedError);
    }

    public String getVersionAndName() {
        return "%s\nCreated by %s".formatted(versionProvider.getString(), "Marvin Kuhnke");
    }

    private static void runTimed(final VoidCallable callable, final String startMessage, final String completeMessage) throws Exception {
        final LocalDateTime start = LocalDateTime.now();
        log.info(startMessage);
        callable.call();
        log.info("%s in %s".formatted(completeMessage, Duration.between(start, LocalDateTime.now())));
    }

    public void downloadNewVersion() {
        hostServices.showDocument(versionResponse.get().getUrl());

        Platform.exit();
        System.exit(0);
    }

    private static class CallableTask extends Task<Void> {
        private final String taskStarted;
        private final String taskCompleted;
        private final VoidCallable callable;

        public CallableTask(final String taskStarted, final String taskCompleted, final VoidCallable callable) {
            this.taskStarted = taskStarted;
            this.taskCompleted = taskCompleted;
            this.callable = callable;
        }

        @Override
        protected Void call() throws Exception {
            runTimed(callable, taskStarted, taskCompleted);
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
            log.debug("Starting preloader tasks");
            final int n = tasks.size();

            for (int i = 0; i < n; i++) {
                final CallableTask task = tasks.get(i);
                final int progressDone = i;
                Platform.runLater(() -> {
                    updateProgress(progressDone, n);
                    currentTaskDescription.set(task.taskStarted);
                });

                task.call();
            }

            Platform.runLater(() -> updateProgress(1, 1));

            log.debug("Completed preloader tasks");
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
        public RepositoryTask(final String taskStarted, final String taskCompleted, final ReloadableRepository<?> repository) {
            super(taskStarted, taskCompleted, repository::reload);
        }
    }

    private static class RepositoryLoader extends Service<Void> {
        private final ReadOnlyStringWrapper currentTaskDescription = new ReadOnlyStringWrapper();
        private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper();
        private final ReadOnlyBooleanWrapper completed = new ReadOnlyBooleanWrapper();

        private final List<CallableTask> tasks;

        @Inject
        public RepositoryLoader(
                final RatingsLoader ratingsLoader,
                @Named("world") final PolygonRepository worldRepository,
                @Named("lakes") final PolygonRepository lakesRepository,
                final AirportRepository airportRepository,
                final ClientRepository clientRepository,
                final FlightInformationRegionRepository flightInformationRegionRepository,
                final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
                final UpperInformationRegionRepository upperInformationRegionRepository,
                final InternationalDateLineRepository internationalDateLineRepository,
                final CountryRepository countryRepository,
                final VatsimApi vatsimApi,
                final Preferences preferences,
                @Named("userLogDir") final Path logDir
        ) {
            final var loadWorld = new CallableTask(
                    "Loading World",
                    "Loaded World",
                    worldRepository::reload
            );
            final var loadLakes = new CallableTask(
                    "Loading Lakes",
                    "Loaded Lakes",
                    lakesRepository::reload
            );
            final var loadRatings = new CallableTask(
                    "Loading Ratings",
                    "Loaded Ratings",
                    ratingsLoader::loadRatings
            );
            final var loadAirports = new RepositoryTask(
                    "Loading Airports",
                    "Loaded Airports",
                    airportRepository
            );
            final var loadInternationalDateLine = new RepositoryTask(
                    "Loading International Date Line",
                    "Loaded International Date Line",
                    internationalDateLineRepository
            );
            final var loadFirs = new RepositoryTask(
                    "Loading Flight Information Regions",
                    "Loaded Flight Information Regions",
                    flightInformationRegionRepository
            );
            final var loadFirbs = new RepositoryTask(
                    "Loading Flight Information Region Boundaries",
                    "Loaded Flight Information Region Boundaries",
                    flightInformationRegionBoundaryRepository
            );
            final var loadUirs = new RepositoryTask(
                    "Loading Upper Information Regions",
                    "Loaded Upper Information Regions",
                    upperInformationRegionRepository
            );
            final var loadCountries = new RepositoryTask(
                    "Loading Countries",
                    "Loaded Countries",
                    countryRepository
            );
            final var loadClients = new RepositoryTask(
                    "Loading Clients",
                    "Loaded Clients",
                    clientRepository
            );
            final var clearCaches = new CallableTask(
                    "Cleaning Caches",
                    "Cleaned Caches",
                    () -> {
                        if (vatsimApi instanceof CachedVatsimApi) {
                            ((CachedVatsimApi) vatsimApi).clear();
                        }
                    });
            final var deletingOldLogs = new CallableTask(
                    "Deleting Old Logs",
                    "Old Logs Deleted",
                    () -> {
                        if (preferences.booleanProperty("general.delete_old_logs").get()) {
                            deleteOldLogs(logDir);
                        }
                    }
            );

            tasks = List.of(
                    loadWorld,
                    loadLakes,
                    loadCountries,
                    loadInternationalDateLine,
                    loadFirs,
                    loadFirbs,
                    loadUirs,
                    loadAirports,
                    loadRatings,
                    loadClients,
                    clearCaches,
                    deletingOldLogs
            );
        }

        private void deleteOldLogs(final Path logDir) throws IOException {
            final List<Path> files = Files.list(logDir).collect(Collectors.toList());
            for (final Path file : files) {
                if (isOlderThan14Days(file)) {
                    Files.delete(file);
                }
            }
        }

        private boolean isOlderThan14Days(final Path e) throws IOException {
            return Duration
                    .ofDays(14)
                    .minus(Duration.between(LocalDateTime.ofInstant(Files.getLastModifiedTime(e)
                                                                         .toInstant(), ZoneId.systemDefault()), LocalDateTime.now()))
                    .isNegative();
        }

        @Override
        protected Task<Void> createTask() {
            final CompositeTask result = new CompositeTask(tasks);
            currentTaskDescription.bind(result.currentTaskDescriptionProperty());
            /////////////////////////////////////////////////////
            // dont remove or progress bindings might be GC'd  //
            /////////////////////////////////////////////////////
            progress.bind(result.progressProperty());
            result.setOnScheduled(e -> completed.set(true));
            /////////////////////////////////////////////////////
            //                                                 //
            /////////////////////////////////////////////////////
            return result;
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
