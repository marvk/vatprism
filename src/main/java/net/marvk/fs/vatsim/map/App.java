package net.marvk.fs.vatsim.map;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import eu.lestard.easydi.EasyDI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.fs.vatsim.api.ExampleDataSource;
import net.marvk.fs.vatsim.api.SimpleVatsimApi;
import net.marvk.fs.vatsim.map.data.ClientViewModel;
import net.marvk.fs.vatsim.map.view.GlobalScope;
import net.marvk.fs.vatsim.map.view.main.MainView;

import java.util.List;
import java.util.stream.Collectors;

public class App extends Application {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final SimpleVatsimApi simpleVatsimApi = new SimpleVatsimApi(new ExampleDataSource());

        final GlobalScope globalScope = new GlobalScope();
        final List<ClientViewModel> clientViewModels =
                simpleVatsimApi.data()
                               .getClients()
                               .stream()
                               .map(ClientViewModel::fromVatsimClient)
                               .collect(Collectors.toList());

        globalScope.getClients().setAll(clientViewModels);

        final var viewTuple =
                FluentViewLoader.fxmlView(MainView.class)
                                .providedScopes(globalScope)
                                .load();

        final EasyDI easyDI = new EasyDI();

        MvvmFX.setCustomDependencyInjector(easyDI::getInstance);

        primaryStage.setScene(new Scene(viewTuple.getView()));
        primaryStage.show();
    }
}
