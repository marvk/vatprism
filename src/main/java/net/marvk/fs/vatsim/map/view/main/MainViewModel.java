package net.marvk.fs.vatsim.map.view.main;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.CompositeCommand;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.api.CachedVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

import java.util.Arrays;

@ScopeProvider({StatusScope.class, ToolbarScope.class, SettingsScope.class})
@Log4j2
public class MainViewModel implements ViewModel {
    private final ReadOnlyStringWrapper style = new ReadOnlyStringWrapper();

    private final DelegateCommand loadClients;

    private final Preferences preferences;

    private static final Duration RELOAD_PERIOD = Duration.seconds(30);
    private final ReloadService clientReloadService;

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public MainViewModel(
            final RatingsLoader ratingsLoader,
            final AirportRepository airportRepository,
            final ClientRepository clientRepository,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final CountryRepository countryRepository,
            final Preferences preferences,
            final VatsimApi vatsimApi
    ) {
        this.preferences = preferences;

        final var loadRatings = new DelegateCommand(() -> new Action() {
            @Override
            protected void action() throws Exception {
                ratingsLoader.loadRatings();
            }
        });
        final var loadAirports = new ReloadRepositoryCommand(airportRepository, false);
        final var loadInternationalDateLine = new ReloadRepositoryCommand(internationalDateLineRepository, false);
        final var loadFirs = new ReloadRepositoryCommand(flightInformationRegionRepository, false);
        final var loadFirbs = new ReloadRepositoryCommand(flightInformationRegionBoundaryRepository, false);
        final var loadUirs = new ReloadRepositoryCommand(upperInformationRegionRepository, false);
        final var loadCountries = new ReloadRepositoryCommand(countryRepository, false);

        this.loadClients = new ReloadRepositoryCommand(clientRepository, true, Notifications.REPAINT::publish);

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
                        System.out.println(Arrays.toString(ControllerRating.values()));
                        System.out.println(Arrays.toString(PilotRating.values()));
                    }
                })
        );
        compositeCommand.execute();

        Notifications.RELOAD_CLIENTS.subscribe(this::reloadClients);

        clientReloadService = new ReloadService(loadClients);
        clientReloadService.setPeriod(RELOAD_PERIOD);
        clientReloadService.setDelay(RELOAD_PERIOD);
    }

    private void reloadClients() {
        loadClients.execute();
    }

    public void initialize() {
        toolbarScope.autoReloadProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                reloadClients();
            }
            setServiceRunning(newValue);
        });

        toolbarScope.reloadRunningProperty().bind(loadClients.runningProperty());
        toolbarScope.reloadExecutableProperty().bind(loadClients.executableProperty());
        toolbarScope.reloadExceptionProperty().bind(loadClients.exceptionProperty());

        final IntegerProperty fontSize = preferences.integerProperty("general.font_size");
        final ObjectProperty<Color> baseColor = preferences.colorProperty("world.fill_color");

        style.bind(Bindings.createStringBinding(this::style, fontSize, baseColor));
    }

    private String style() {
        final IntegerProperty fontSize = preferences.integerProperty("general.font_size");
        final Color baseColor = preferences.colorProperty("world.fill_color").get();

        final String fontSizeStyle = "-fx-font-size: %spx;".formatted(fontSize.get());

        if (baseColor == null) {
            return fontSizeStyle;
        } else {
            final double b = baseColor.getBrightness();
            return fontSizeStyle + """
                    -vatsim-background-color: #%s;
                    -vatsim-background-color-light: #%s;
                    -vatsim-background-color-very-light: #%s;
                    -vatsim-background-color-very-light-25: #%s;
                    -vatsim-background-color-very-very-light: #%s;
                    -vatsim-background-color-very-very-very-light: #%s;
                                        
                    -vatsim-text-color-dark: #%s;
                    -vatsim-text-color: #%s;
                    -vatsim-text-color-light: #%s;
                    -vatsim-text-greyed-out: #%s;
                    """.formatted(
                    (Object[]) fixColorStrings(
                            baseColor,
                            baseColor.deriveColor(0, 1.00, mapBrightnessFactor(b, 1.50), 1.00),
                            baseColor.deriveColor(0, 0.80, mapBrightnessFactor(b, 2.00), 1.00),
                            baseColor.deriveColor(0, 0.50, mapBrightnessFactor(b, 2.00), 0.25),
                            baseColor.deriveColor(0, 0.30, mapBrightnessFactor(b, 2.50), 1.00),
                            baseColor.deriveColor(0, 0.20, mapBrightnessFactor(b, 3.00), 1.00),

                            baseColor.deriveColor(0, 0.70, mapBrightnessFactor(b, 6.00), 1.00),
                            baseColor.deriveColor(0, 0.60, mapBrightnessFactor(b, 7.50), 1.00),
                            baseColor.deriveColor(0, 0.50, mapBrightnessFactor(b, 10.00), 1.00),
                            baseColor.deriveColor(0, 0.00, mapBrightnessFactor(b, 7.50), 1.00)
                    )
            );
        }
    }

    private static double mapBrightnessFactor(final double brightness, final double value) {
        if (brightness > 0.5) {
            return 1.0 / value;
        } else {
            return value;
        }
    }

    private String[] fixColorStrings(final Color... colors) {
        return Arrays.stream(colors).map(Color::toString).map(e -> e.substring(2, 8)).toArray(String[]::new);
    }

    private void setServiceRunning(final boolean running) {
        if (running) {
            clientReloadService.setStopping(false);
            if (!clientReloadService.isRunning()) {
                log.info("Starting reload service");
                clientReloadService.start();
            }
        } else {
            log.info("Resetting reload service");
            clientReloadService.setStopping(true);
        }
    }

    public String getStyle() {
        return style.get();
    }

    public ReadOnlyStringProperty styleProperty() {
        return style.getReadOnlyProperty();
    }

    private static final class ReloadRepositoryCommand extends DelegateCommand {
        private ReloadRepositoryCommand(final ReloadableRepository<?> repository, final boolean background) {
            this(repository, background, null);
        }

        private ReloadRepositoryCommand(final ReloadableRepository<?> repository, final boolean background, final Runnable onSucceed) {
            super(() -> new ReloadRepositoryAction(repository, onSucceed), new ImmutableObjectProperty<>(true), background);
        }

        private static final class ReloadRepositoryAction extends Action {
            private final ReloadableRepository<?> repository;
            private final Runnable onSucceed;

            public ReloadRepositoryAction(final ReloadableRepository<?> repository, final Runnable onSucceed) {
                this.repository = repository;
                this.onSucceed = onSucceed;
            }

            @Override
            protected void action() throws Exception {
                updateProgress(0, 1);
                repository.reloadAsync(onSucceed);
                updateProgress(1, 1);
            }
        }
    }

    private static class ReloadService extends ScheduledService<Void> {
        private final Command command;

        private boolean stopping = false;

        public ReloadService(final Command command) {
            this.command = command;
        }

        @Override
        protected Task<Void> createTask() {
            return new ReloadTask();
        }

        public void setStopping(final boolean stopping) {
            this.stopping = stopping;
        }

        private class ReloadTask extends Task<Void> {
            @Override
            protected Void call() {
                if (!stopping) {
                    Platform.runLater(command::execute);
                }
                return null;
            }
        }
    }
}
