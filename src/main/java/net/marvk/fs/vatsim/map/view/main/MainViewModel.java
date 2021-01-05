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

    private final DelegateCommand loadAirports;
    private final DelegateCommand loadFirs;
    private final DelegateCommand loadFirbs;
    private final DelegateCommand loadUirs;
    private final DelegateCommand loadClients;
    private final DelegateCommand loadInternationalDateLine;
    private final ReloadRepositoryCommand loadCountries;
    private final Preferences preferences;

    private static final Duration RELOAD_PERIOD = Duration.seconds(30);
    private final ReloadService clientReloadService;

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public MainViewModel(
            final AirportRepository airportRepository,
            final ClientRepository clientRepository,
            final FlightInformationRegionRepository flightInformationRegionRepository,
            final FlightInformationRegionBoundaryRepository flightInformationRegionBoundaryRepository,
            final UpperInformationRegionRepository upperInformationRegionRepository,
            final InternationalDateLineRepository internationalDateLineRepository,
            final CountryRepository countryRepository,
            final Preferences preferences
    ) {
        this.preferences = preferences;

        this.loadAirports = new ReloadRepositoryCommand(airportRepository, false);
        this.loadInternationalDateLine = new ReloadRepositoryCommand(internationalDateLineRepository, false);
        this.loadFirs = new ReloadRepositoryCommand(flightInformationRegionRepository, false);
        this.loadFirbs = new ReloadRepositoryCommand(flightInformationRegionBoundaryRepository, false);
        this.loadUirs = new ReloadRepositoryCommand(upperInformationRegionRepository, false);
        this.loadCountries = new ReloadRepositoryCommand(countryRepository, false);
        this.loadClients = new ReloadRepositoryCommand(clientRepository, true, this::triggerRepaint);

        final CompositeCommand compositeCommand = new CompositeCommand(
                loadInternationalDateLine,
                loadCountries,
                loadFirs,
                loadFirbs,
                loadUirs,
                loadAirports,
                loadClients
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

    private void triggerRepaint() {
        Notifications.REPAINT.publish();
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
        final ObjectProperty<Color> baseColor = preferences.colorProperty("world.color");

        style.bind(Bindings.createStringBinding(this::style, fontSize, baseColor));
    }

    private String style() {
        final IntegerProperty fontSize = preferences.integerProperty("general.font_size");
        final Color baseColor = preferences.colorProperty("world.color").get();

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
                            baseColor.deriveColor(0, 1, mapBrightnessFactor(b, 1.5), 1),
                            baseColor.deriveColor(0, 0.95, mapBrightnessFactor(b, 2), 1),
                            baseColor.deriveColor(0, 0.95, mapBrightnessFactor(b, 2), 0.25),
                            baseColor.deriveColor(0, 0.9, mapBrightnessFactor(b, 2.5), 1),
                            baseColor.deriveColor(0, 0.85, mapBrightnessFactor(b, 3), 1),

                            baseColor.deriveColor(0, 0.7, mapBrightnessFactor(b, 4), 1),
                            baseColor.deriveColor(0, 0.6, mapBrightnessFactor(b, 4.5), 1),
                            baseColor.deriveColor(0, 0.5, mapBrightnessFactor(b, 5), 1),
                            baseColor.deriveColor(0, 0, mapBrightnessFactor(b, 4.5), 1)
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
        private ReloadRepositoryCommand(final Repository<?> repository, final boolean background) {
            this(repository, background, null);
        }

        private ReloadRepositoryCommand(final Repository<?> repository, final boolean background, final Runnable onSucceed) {
            super(() -> new ReloadRepositoryAction(repository, onSucceed), new ImmutableObjectProperty<>(true), background);
        }

        private static final class ReloadRepositoryAction extends Action {
            private final Repository<?> repository;
            private final Runnable onSucceed;

            public ReloadRepositoryAction(final Repository<?> repository, final Runnable onSucceed) {
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
