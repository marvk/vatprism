package net.marvk.fs.vatsim.map.view.clients;

import de.saxsys.mvvmfx.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.marvk.fs.vatsim.map.data.ClientViewModel;
import net.marvk.fs.vatsim.map.view.clientdetail.ClientDetailView;
import org.controlsfx.control.table.TableFilter;

public class ClientsView implements FxmlView<ClientsViewModel> {
    @FXML
    private SplitPane splitPane;

    @FXML
    private TableView<ClientViewModel> table;

    @InjectViewModel
    private ClientsViewModel viewModel;

    @InjectContext
    private Context context;

    public void initialize() {
        table.setItems(viewModel.getClients());

        viewModel.getClients().addListener(new ListChangeListener<ClientViewModel>() {
            @Override
            public void onChanged(final Change<? extends ClientViewModel> c) {
                System.out.println(c);
            }
        });

        System.out.println(viewModel.getClients());

        addColumn("Callsign", "callsign");
        addColumn("Name", "realName");
        addColumn("Type", "clientType");

        table.setFixedCellSize(17);

        TableFilter.forTableView(table).apply();

        final var selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        viewModel.selectedClientProperty().bind(selectionModel.selectedItemProperty());

        instantiateDetailView();
    }

    private void instantiateDetailView() {
        final var viewTuple =
                FluentViewLoader.fxmlView(ClientDetailView.class)
                                .context(context)
                                .load();

        viewModel.selectedClientProperty().addListener((observable, oldValue, newValue) -> viewTuple.getViewModel().getClient().importData(newValue));

        splitPane.getItems().add(viewTuple.getView());
    }

    private boolean addColumn(final String header, final String propertyName) {
        final TableColumn<ClientViewModel, String> column = new TableColumn<>(header);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return table.getColumns().add(column);
    }
}
