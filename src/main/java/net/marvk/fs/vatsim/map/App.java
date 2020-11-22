package net.marvk.fs.vatsim.map;

import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.MvvmFX;
import eu.lestard.easydi.EasyDI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.marvk.fs.vatsim.api.HttpDataSource;
import net.marvk.fs.vatsim.api.SimpleVatsimApi;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.map.repository.AirportsRepository;
import net.marvk.fs.vatsim.map.repository.ClientRepository;
import net.marvk.fs.vatsim.map.view.GlobalScope;
import net.marvk.fs.vatsim.map.view.main.MainView;

public class App extends Application {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        final GlobalScope globalScope = new GlobalScope();

        final EasyDI easyDI = new EasyDI();

        final VatsimApi vatsimApi = new SimpleVatsimApi(new HttpDataSource());
        easyDI.bindInstance(VatsimApi.class, vatsimApi);
        final ClientRepository clientRepository = new ClientRepository(vatsimApi);
        clientRepository.reload();
        easyDI.bindInstance(ClientRepository.class, clientRepository);
        final AirportsRepository airportsRepository = new AirportsRepository(vatsimApi);
        airportsRepository.reload();
        easyDI.bindInstance(AirportsRepository.class, airportsRepository);

        MvvmFX.setCustomDependencyInjector(easyDI::getInstance);

        final var viewTuple =
                FluentViewLoader.fxmlView(MainView.class)
                                .providedScopes(globalScope)
                                .load();

        primaryStage.setScene(new Scene(viewTuple.getView()));
        primaryStage.show();
    }
}
