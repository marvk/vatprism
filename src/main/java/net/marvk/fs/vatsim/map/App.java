package net.marvk.fs.vatsim.map;

import com.google.inject.Module;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.ViewTuple;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.configuration.*;
import net.marvk.fs.vatsim.map.view.main.MainView;
import net.marvk.fs.vatsim.map.view.main.MainViewModel;
import net.marvk.fs.vatsim.map.view.preloader.PreloaderView;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.io.IoBuilder;

import java.util.List;

@Log4j2
public class App extends MvvmfxGuiceApplication {

    public static void main(final String[] args) {
        System.setErr(IoBuilder.forLogger("System.err").setLevel(Level.ERROR).buildPrintStream());
//        System.setOut(IoBuilder.forLogger("System.out").setLevel(Level.TRACE).buildPrintStream());
        launch(args);
    }

    @Override
    public void startMvvmfx(final Stage primaryStage) {
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

        loadFonts();
        startPreloader(primaryStage);
    }

    private void startPreloader(final Stage preloaderStage) {
        final var viewTuple = FluentViewLoader.fxmlView(PreloaderView.class).load();

        viewTuple.getViewModel().viewTupleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                preloaderStage.close();
                App.this.showMainView(newValue);
            }
        });

        preloaderStage.setScene(new Scene(viewTuple.getView()));
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.setAlwaysOnTop(true);
        preloaderStage.show();
        viewTuple.getViewModel().load();
    }

    private void showMainView(final ViewTuple<MainView, MainViewModel> viewTuple) {
        final Stage secondaryStage = new Stage(StageStyle.DECORATED);

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
                secondaryStage.setFullScreen(!secondaryStage.isFullScreen());
            }
        });
        secondaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        secondaryStage.getIcons().addAll(loadIcon("icon-16.png"), loadIcon("icon-24.png"), loadIcon("icon-32.png"));
        secondaryStage.setTitle("VATprism");
        log.info("Creating scene");
        secondaryStage.setScene(new Scene(viewTuple.getView(), 1366, 768));
        log.info("Showing stage");
        secondaryStage.show();
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
        modules.add(new AppModule());
        modules.add(new AopModule());
        modules.add(new JfxModule());
        modules.add(new PathsModule());
        modules.add(new MetarModule());
        modules.add(new VersionModule());
    }
}
