package net.marvk.fs.vatsim.map.view.main;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
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
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.data.ImmutableObjectProperty;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.ReloadableRepository;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.filter.FilterScope;

import java.util.Arrays;

@ScopeProvider({StatusScope.class, ToolbarScope.class, SettingsScope.class, FilterScope.class})
@Log4j2
public class MainViewModel implements ViewModel {
    private final ReadOnlyStringWrapper style = new ReadOnlyStringWrapper();

    private final DelegateCommand loadClientsAsync;

    private final Preferences preferences;

    private static final Duration RELOAD_PERIOD = Duration.seconds(30);
    private final ReloadService clientReloadService;

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public MainViewModel(
            final ClientRepository clientRepository,
            final Preferences preferences
    ) {
        this.preferences = preferences;

        Notifications.RELOAD_CLIENTS.subscribe(this::reloadClients);

        this.loadClientsAsync = new ReloadRepositoryCommand(clientRepository, this::clientReloadCompleted);
        clientReloadService = new ReloadService(loadClientsAsync);
        clientReloadService.setPeriod(RELOAD_PERIOD);
        clientReloadService.setDelay(RELOAD_PERIOD);
    }

    private void clientReloadCompleted() {
        Notifications.REPAINT.publish();
        Notifications.CLIENTS_RELOADED.publish();
    }

    private void reloadClients() {
        loadClientsAsync.execute();
    }

    public void initialize() {
        toolbarScope.autoReloadProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                reloadClients();
            }
            setServiceRunning(newValue);
        });

        toolbarScope.reloadRunningProperty().bind(loadClientsAsync.runningProperty());
        toolbarScope.reloadExecutableProperty().bind(loadClientsAsync.executableProperty());
        toolbarScope.reloadExceptionProperty().bind(loadClientsAsync.exceptionProperty());

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
                            baseColor.deriveColor(0, 1.00, mapBrightnessFactor(baseColor, 1.50), 1.00),
                            baseColor.deriveColor(0, 0.80, mapBrightnessFactor(baseColor, 2.00), 1.00),
                            baseColor.deriveColor(0, 0.50, mapBrightnessFactor(baseColor, 2.00), 0.25),
                            baseColor.deriveColor(0, 0.30, mapBrightnessFactor(baseColor, 2.50), 1.00),
                            baseColor.deriveColor(0, 0.20, mapBrightnessFactor(baseColor, 3.00), 1.00),

                            baseColor.deriveColor(0, 0.70, mapBrightnessFactor(baseColor, 6.00), 1.00),
                            baseColor.deriveColor(0, 0.60, mapBrightnessFactor(baseColor, 7.50), 1.00),
                            baseColor.deriveColor(0, 0.50, mapBrightnessFactor(baseColor, 10.00), 1.00),
                            baseColor.deriveColor(0, 0.00, mapBrightnessFactor(baseColor, 7.50), 1.00)
                    )
            );
        }
    }

    private double mapBrightnessFactor(final Color originalColor, final double value) {
        if (luminance(originalColor.brighter().brighter()) > 0.5) {
            return 1.0 / value;
        } else {
            return value;
        }
    }

    private double luminance(final Color c) {
        return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
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

    @Log4j2
    public static final class ReloadRepositoryCommand extends DelegateCommand {
        public ReloadRepositoryCommand(final ReloadableRepository<?> repository) {
            this(repository, null);
        }

        public ReloadRepositoryCommand(final ReloadableRepository<?> repository, final Runnable onSucceed) {
            super(() -> new ReloadRepositoryAction(repository, onSucceed), new ImmutableObjectProperty<>(true), onSucceed != null);
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
                if (onSucceed != null) {
                    log.info("Loading %s".formatted(repository.getClass().getSimpleName()));
                    repository.reloadAsync(onSucceed);
                } else {
                    log.info("Async Loading %s".formatted(repository.getClass().getSimpleName()));
                    repository.reload();
                }
                updateProgress(1, 1);
            }
        }
    }
}
