package net.marvk.fs.vatsim.map;

import com.google.inject.Module;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.guice.MvvmfxGuiceApplication;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.fs.vatsim.map.configuration.AopModule;
import net.marvk.fs.vatsim.map.configuration.AppModule;
import net.marvk.fs.vatsim.map.view.main.MainView;

import java.util.List;

public class App extends MvvmfxGuiceApplication {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void startMvvmfx(final Stage primaryStage) {
        final var viewTuple = FluentViewLoader
                .fxmlView(MainView.class)
                .load();

//        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.setScene(new Scene(viewTuple.getView(), 1366, 768));
        primaryStage.show();
    }

    @Override
    public void initGuiceModules(final List<Module> modules) {
        modules.add(new AppModule());
        modules.add(new AopModule());
    }
}
