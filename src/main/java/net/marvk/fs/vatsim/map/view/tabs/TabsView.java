package net.marvk.fs.vatsim.map.view.tabs;

import de.saxsys.mvvmfx.*;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import net.marvk.fs.vatsim.map.view.clients.ClientsView;

public class TabsView implements FxmlView<TabsViewModel> {
    @FXML
    private TabPane tabPane;

    @InjectViewModel
    private TabsViewModel viewModel;

    @InjectContext
    private Context context;

    public void initialize() {
        final var viewTuple = FluentViewLoader.fxmlView(ClientsView.class).context(context).load();

        tabPane.getTabs().add(new Tab("Clients", viewTuple.getView()));
    }
}
