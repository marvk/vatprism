package net.marvk.fs.vatsim.map;

import com.google.inject.Module;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.fs.vatsim.map.view.GlobalScope;
import net.marvk.fs.vatsim.map.view.main.MainView;

import java.util.List;

public class App extends MvvmfxGuiceApplication {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void startMvvmfx(final Stage primaryStage) throws Exception {
        final GlobalScope globalScope = new GlobalScope();

        final var viewTuple =
                FluentViewLoader.fxmlView(MainView.class)
                                .providedScopes(globalScope)
                                .load();

        primaryStage.setScene(new Scene(viewTuple.getView()));
        primaryStage.show();
    }

    @Override
    public void initGuiceModules(final List<Module> modules) throws Exception {
        modules.add(new AppModule());
    }
}
