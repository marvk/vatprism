package net.marvk.fs.vatsim.map.view.main;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ScopeProvider;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.Action;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.StatusScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.filter.FilterScope;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ScopeProvider({StatusScope.class, ToolbarScope.class, SettingsScope.class, FilterScope.class})
@Log4j2
public class MainViewModel implements ViewModel {
    private final ReadOnlyStringWrapper style = new ReadOnlyStringWrapper();

    private final DelegateCommand loadClientsAsync;

    private final Preferences preferences;

    private final ReloadService clientReloadService;

    private final ReadOnlyBooleanWrapper onboarding = new ReadOnlyBooleanWrapper();

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public MainViewModel(
            final ClientRepository clientRepository,
            final Preferences preferences,
            final VersionProvider versionProvider,
            @Named("vatsimApiRefreshRate") final Duration refreshRate
    ) {
        this.preferences = preferences;

        Notifications.RELOAD_CLIENTS.subscribe(this::reloadClients);

        loadClientsAsync = new ReloadRepositoryCommand(clientRepository, this::clientReloadCompleted);
        clientReloadService = new ReloadService(loadClientsAsync);
        clientReloadService.setPeriod(javafx.util.Duration.seconds(refreshRate.getSeconds()));
        clientReloadService.setDelay(javafx.util.Duration.seconds(refreshRate.getSeconds()));

        final StringProperty version = preferences.stringProperty("meta.version");
        if (version.get() == null || "0.0.0".equals(version.get())) {
            onboarding.set(true);
        }

        version.set(versionProvider.getString());
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

        style.bind(new StyleBindingGenerator(preferences).styleBinding());
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

    public boolean isOnboarding() {
        return onboarding.get();
    }

    public ReadOnlyBooleanProperty onboardingProperty() {
        return onboarding.getReadOnlyProperty();
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

    private static class StyleBindingGenerator {
        private final IntegerProperty fontSize;
        private final BooleanProperty autoColor;
        private final BooleanProperty autoShade;
        private final ObjectProperty<Color> backgroundColor;
        private final BooleanProperty backgroundShadingInverted;
        private final BooleanProperty backgroundShadingReversed;
        private final ObjectProperty<Color> textColor;
        private final BooleanProperty textShadingInverted;
        private final ObjectProperty<Color> worldColor;

        public StyleBindingGenerator(final Preferences preferences) {
            this.fontSize = preferences.integerProperty("general.font_size");
            this.autoColor = preferences.booleanProperty("ui.auto_color");
            this.autoShade = preferences.booleanProperty("ui.auto_shade");
            this.backgroundColor = preferences.colorProperty("ui.background_base_color");
            this.backgroundShadingInverted = preferences.booleanProperty("ui.invert_background_shading");
            this.backgroundShadingReversed = preferences.booleanProperty("ui.reverse_background_shading");
            this.textColor = preferences.colorProperty("ui.text_base_color");
            this.textShadingInverted = preferences.booleanProperty("ui.invert_text_shading");
            this.worldColor = preferences.colorProperty("world.fill_color");
        }

        public ObservableValue<String> styleBinding() {
            return Bindings.createStringBinding(this::style,
                    fontSize,
                    autoColor,
                    autoShade,
                    backgroundColor,
                    backgroundShadingInverted,
                    backgroundShadingReversed,
                    textColor,
                    textShadingInverted,
                    worldColor
            );
        }

        private String style() {
            final Color backgroundBase = autoColor.get() ? worldColor.get() : backgroundColor.get();
            final Color textBase = autoColor.get() ? worldColor.get() : textColor.get();

            final String fontSizeStyle = "-fx-font-size: %spx;".formatted(fontSize.get());

            if (backgroundBase == null) {
                return fontSizeStyle;
            } else {
                final double luminance = luminance(backgroundBase.brighter().brighter());
                final boolean bright = luminance > 0.5;

                final boolean reverseBackgroundShading = (autoShade.get() && bright) || (!autoShade.get() && backgroundShadingReversed.get());
                final boolean invertBackgroundShading = (autoShade.get() && bright) || (!autoShade.get() && backgroundShadingInverted.get());
                final boolean invertTextShading = !autoShade.get() && textShadingInverted.get();

                final double brightnessFactor = Math.min(1, Math.max(0.3, 1.2 - luminance * 1.5));

                final List<String> colors = fixColorStrings(
                        backgroundBase,
                        backgroundBase.deriveColor(0, 1.00, mapBrightnessFactor(1 + 0.50 * brightnessFactor, invertBackgroundShading), 1.00),
                        backgroundBase.deriveColor(0, 0.80, mapBrightnessFactor(1 + 1.00 * brightnessFactor, invertBackgroundShading), 1.00),
                        backgroundBase.deriveColor(0, 0.50, mapBrightnessFactor(1 + 1.00 * brightnessFactor, invertBackgroundShading), 0.25),
                        backgroundBase.deriveColor(0, 0.30, mapBrightnessFactor(1 + 1.50 * brightnessFactor, invertBackgroundShading), 1.00),
                        backgroundBase.deriveColor(0, 0.20, mapBrightnessFactor(1 + 2.00 * brightnessFactor, invertBackgroundShading), 1.00),

                        textBase.deriveColor(0, 0.70, mapBrightnessFactor(6.00, invertTextShading), 1.00),
                        textBase.deriveColor(0, 0.60, mapBrightnessFactor(7.50, invertTextShading), 1.00),
                        textBase.deriveColor(0, 0.50, mapBrightnessFactor(10.00, invertTextShading), 1.00),
                        textBase.deriveColor(0, 0.00, mapBrightnessFactor(7.50, invertTextShading), 1.00)
                );

                if (reverseBackgroundShading) {
                    Collections.swap(colors, 0, 5);
                    Collections.swap(colors, 1, 4);
                }

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
                        colors.toArray()
                );
            }
        }

        private static double mapBrightnessFactor(final double value, final boolean bright) {
            if (bright) {
                return 1.0 / value;
            } else {
                return value;
            }
        }

        /**
         * @see <a href="https://alienryderflex.com/hsp.html">HSP Color Model — Alternative to HSV (HSB) and HSL</a>
         */
        private static double luminance(final Color c) {
            final double r = c.getRed();
            final double g = c.getGreen();
            final double b = c.getBlue();

            return Math.sqrt(0.299 * r * r + 0.587 * g * g + 0.114 * b * b);
        }

        private static List<String> fixColorStrings(final Color... colors) {
            return Arrays.stream(colors)
                         .map(Color::toString)
                         .map(e -> e.substring(2, 8))
                         .collect(Collectors.toCollection(ArrayList::new));
        }
    }
}
