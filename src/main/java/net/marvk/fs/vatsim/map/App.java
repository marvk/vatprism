package net.marvk.fs.vatsim.map;

import com.google.inject.Module;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.configuration.AopModule;
import net.marvk.fs.vatsim.map.configuration.AppModule;
import net.marvk.fs.vatsim.map.view.main.MainView;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.List;

@Log4j2
public class App extends MvvmfxGuiceApplication {

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void startMvvmfx(final Stage primaryStage) {
        final Parameters parameters = getParameters();

        final String logLevelString = parameters.getNamed().get("loglevel");
        if (logLevelString != null) {
            final Level level = Level.toLevel(logLevelString, null);

            if (level != null) {
                Configurator.setRootLevel(level);
            } else {
                log.warn("Failed to set log level to \"%s\"".formatted(logLevelString));
            }
        }

        final var viewTuple = FluentViewLoader
                .fxmlView(MainView.class)
                .load();

        // TODO Ugly hack to stay in front...
        primaryStage.focusedProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        if (!newValue && primaryStage.isFullScreen()) {
                            Platform.runLater(primaryStage::toFront);
                        }
                    });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyCode.F11 == event.getCode()) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
            }
        });
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.getIcons().addAll(loadIcon("icon-16.png"), loadIcon("icon-32.png"));
        primaryStage.setTitle("VATSim Map");
        primaryStage.setScene(new Scene(viewTuple.getView(), 1366, 768));
        primaryStage.show();
    }

    private Image loadIcon(final String name) {
        return new Image(getClass().getResourceAsStream("/net/marvk/fs/vatsim/map/" + name));
    }

    @Override
    public void initGuiceModules(final List<Module> modules) {
        modules.add(new AppModule());
        modules.add(new AopModule());
    }
}
