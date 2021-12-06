package net.marvk.fs.vatsim.map;

import com.google.inject.Module;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.configuration.*;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.Writable;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.main.MainView;
import net.marvk.fs.vatsim.map.view.main.MainViewModel;
import net.marvk.fs.vatsim.map.view.onboarding.OnboardingView;
import net.marvk.fs.vatsim.map.view.preloader.PreloaderView;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.io.IoBuilder;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Log4j2
public class App extends MvvmfxGuiceApplication {

    public static void main(final String[] args) {
        System.setErr(IoBuilder.forLogger("System.err").setLevel(Level.ERROR).buildPrintStream());
//        System.setOut(IoBuilder.forLogger("System.out").setLevel(Level.TRACE).buildPrintStream());
        launch(args);
    }

    @Override
    public void startMvvmfx(final Stage primaryStage) {
        loadFonts();
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle("vatprism", new Locale("en", "us")));
        startPreloader(primaryStage);
    }

    private void startPreloader(final Stage preloaderStage) {
        final var viewTuple = FluentViewLoader.fxmlView(PreloaderView.class).load();

        viewTuple.getViewModel().viewTupleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                preloaderStage.close();
                App.this.postPreloader(newValue);
            }
        });

        preloaderStage.setScene(new Scene(viewTuple.getView()));
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.setAlwaysOnTop(true);
        preloaderStage.show();
        viewTuple.getViewModel().load();
    }

    @SneakyThrows
    private void postPreloader(final ViewTuple<MainView, MainViewModel> viewTuple) {
        if (viewTuple.getViewModel().isOnboarding()) {
            showOnboardingView(() -> showMainView(viewTuple));
        } else {
            showMainView(viewTuple);
        }
    }

    private void showMainView(final ViewTuple<MainView, MainViewModel> viewTuple) {
        final Stage secondaryStage = new Stage(StageStyle.DECORATED);

        secondaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F && event.isControlDown()) {
                Notifications.SEARCH.publish();
            }
        });

        log.info("Loading view");

        log.info("Configuring stage");
        // TODO Ugly hack to stay in front...
        secondaryStage.focusedProperty()
                      .addListener((observable, oldValue, newValue) -> {
                          if (!newValue && secondaryStage.isFullScreen()) {
                              Platform.runLater(secondaryStage::toFront);
                          }
                      });

        secondaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.F11 == event.getCode()) {
                final boolean value = !secondaryStage.isFullScreen();
                setFullscreen(secondaryStage, value);
            }
        });
        final Preferences preferences = DependencyInjector.getInstance().getInstanceOf(Preferences.class);

        secondaryStage.setOnCloseRequest(e -> {
            setWindowSizeAndPosition(secondaryStage, preferences);
            if (preferences instanceof Writable) {
                ((Writable) preferences).write();
            }
            Platform.exit();
            System.exit(0);
        });

        secondaryStage.getIcons().addAll(loadIcon("icon-16.png"), loadIcon("icon-24.png"), loadIcon("icon-32.png"));
        secondaryStage.setTitle("VATprism");
        log.info("Creating scene");
        secondaryStage.setScene(new Scene(viewTuple.getView(), 1366, 768));
        log.info("Showing stage");

        secondaryStage.show();
        setStageSizeAndPositionFromPreferences(secondaryStage, preferences);
    }

    private static void setFullscreen(final Stage secondaryStage, final boolean value) {
        secondaryStage.setFullScreen(value);
        secondaryStage.setAlwaysOnTop(value);
    }

    private static void setStageSizeAndPositionFromPreferences(final Stage stage, final Preferences preferences) {
        final BooleanProperty nextStartupIsValidProperty = preferences.booleanProperty("window.nextStartupIsValid", false);
        final boolean rememberLastPosition = preferences.booleanProperty("window.rememberLastPosition", false).get();
        final boolean preferencesAreValid = nextStartupIsValidProperty.get();

        final WindowParameters windowParameters = WindowParameters.readFromPreferences(preferences);

        if (rememberLastPosition && preferencesAreValid) {
            windowParameters.setToStage(stage);
            log.info("Set screen parameters to " + windowParameters.logString());
        } else if (!preferencesAreValid) {
            log.info("Read invalid last parameters " + windowParameters.logString());
        } else {
            log.debug("User did not enable remembering last position, skipping setting window to parameters " + windowParameters.logString());
        }
        nextStartupIsValidProperty.set(false);
    }

    private static void setWindowSizeAndPosition(final Stage stage, final Preferences preferences) {
        final WindowParameters windowParameters = WindowParameters.readFromStage(stage);
        windowParameters.writeToPreferences(preferences);
        log.info("Wrote to preferences the window parameters " + windowParameters.logString());
        preferences.booleanProperty("window.nextStartupIsValid").set(true);
    }

    private void showOnboardingView(final Runnable showMainView) {
        final Stage stage = new Stage(StageStyle.UNDECORATED);

        final var viewTuple = FluentViewLoader.fxmlView(OnboardingView.class).load();

        stage.setScene(new Scene(viewTuple.getView()));
        stage.show();

        viewTuple.getViewModel().onboardingCompleteProperty().addListener((observable, oldValue, newValue) -> {
            stage.close();
            showMainView.run();
        });
    }

    private void loadFonts() {
        final String path = "/net/marvk/fs/vatsim/map/view/fonts/";
        log.info("Loading fonts from %s".formatted(path));
        final List<String> fontFiles = List.of("B612-Bold.ttf",
                "B612-BoldItalic.ttf",
                "B612-Italic.ttf",
                "B612-Regular.ttf",
                "B612Mono-Bold.ttf",
                "B612Mono-BoldItalic.ttf",
                "B612Mono-Italic.ttf",
                "B612Mono-Regular.ttf",
                "vatprism.ttf"
        );

        for (final String fontFile : fontFiles) {
            final String absolutePath = path + fontFile;
            log.debug("Loading font %s".formatted(absolutePath));
            Font.loadFont(getClass().getResourceAsStream(absolutePath), -1);
        }
    }

    private Image loadIcon(final String name) {
        return new Image(getClass().getResourceAsStream("/net/marvk/fs/vatsim/map/" + name));
    }

    @Override
    public void initGuiceModules(final List<Module> modules) {
        // Call this here because initMvvmfx is called after initGuiceModules and we might miss some log messages
        setLogLevel();

        modules.add(new AppModule());
        modules.add(new JfxModule());
        modules.add(new PathsModule());
        modules.add(new MetarModule());
        modules.add(new ApiModule());
    }

    private void setLogLevel() {
        final Parameters parameters = getParameters();
        log.info("Received parameters %s".formatted(parameters.getRaw()));
        final String logLevelString = parameters.getNamed().get("loglevel");
        if (logLevelString != null) {
            final Level level = Level.toLevel(logLevelString, null);

            if (level != null) {
                log.info("Setting log level to %s from parameters".formatted(level));
                Configurator.setRootLevel(level);
            } else {
                log.warn("Failed to set log level to \"%s\"".formatted(logLevelString));
            }
        }
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class WindowParameters {
        double x;
        double y;
        double width;
        double height;
        boolean fullscreen;
        boolean maximized;

        public String logString() {
            return "x = %f, y = %f, width = %f, height = %f, fullscreen = %b, maximized = %b".formatted(x, y, width, height, fullscreen, maximized);
        }

        public void setToStage(final Stage stage) {
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(width);
            stage.setHeight(height);
            if (fullscreen) {
                // Delay setting fullscreen, because otherwise the Platform will move the fullscreen window to the primary screen
                Platform.runLater(() -> {
                    setFullscreen(stage, true);
                });
            }
            if (maximized) {
                stage.setMaximized(true);
            }
        }

        public void writeToPreferences(final Preferences preferences) {
            preferences.doubleProperty("window.x").set(x);
            preferences.doubleProperty("window.y").set(y);
            preferences.doubleProperty("window.width").set(width);
            preferences.doubleProperty("window.height").set(height);
            preferences.booleanProperty("window.fullscreen").set(fullscreen);
            preferences.booleanProperty("window.maximized").set(maximized);
        }

        public static WindowParameters readFromPreferences(final Preferences preferences) {
            return new WindowParameters(
                    preferences.doubleProperty("window.x").get(),
                    preferences.doubleProperty("window.y").get(),
                    preferences.doubleProperty("window.width").get(),
                    preferences.doubleProperty("window.height").get(),
                    preferences.booleanProperty("window.fullscreen").get(),
                    preferences.booleanProperty("window.maximized").get()
            );
        }

        public static WindowParameters readFromStage(final Stage stage) {
            return new WindowParameters(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), stage.isFullScreen(), stage.isMaximized());
        }
    }
}
