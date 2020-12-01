package net.marvk.fs.vatsim.map.view.clients;

import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.table.AbstractTableView;

public class ClientsView extends AbstractTableView<ClientsViewModel, Client> {
    @FXML
    private SplitPane splitPane;

    @InjectContext
    private Context context;

    @Override
    public void initialize() {
        super.initialize();
        addColumn("Callsign", "callsign");
        addColumn("CID", "cid");
//        addColumn("ControllerType", e -> e.controllerData().controllerTypeProperty().asString());
        addColumn("Name", "realName");
//        addColumn("Type", "rawClientType");

        enableFilter();

        table.setFixedCellSize(18);

//        instantiateDetailView();
    }

//    private void instantiateDetailView() {
//        final var viewTuple =
//                FluentViewLoader.fxmlView(ClientDetailView.class)
//                                .context(context)
//                                .load();
//
//        viewTuple.getViewModel().getClient().modelProperty().bind(Bindings.createObjectBinding(
//                () -> {
//                    if (viewModel.getSelectedItem() != null) {
//                        return viewModel.getSelectedItem().getModel();
//                    }
//                    return null;
//                }, viewModel.selectedItemProperty()
//        ));
//
//        splitPane.getItems().add(viewTuple.getView());
//    }
}
