package net.marvk.fs.vatsim.map.view.table;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.controlsfx.control.table.TableFilter;

public abstract class AbstractTableView<TableViewModel extends AbstractTableViewModel<TableViewViewModel>, TableViewViewModel> implements FxmlView<TableViewModel> {
    @FXML
    private TableView<TableViewViewModel> table;

    @InjectViewModel
    protected TableViewModel viewModel;

    public void initialize() {
        table.setItems(viewModel.items());

        final var selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        viewModel.selectedItemProperty().bind(selectionModel.selectedItemProperty());
    }

    protected void enableFilter() {
        TableFilter.forTableView(table).apply();
    }

    protected void addColumn(final String header, final String propertyName) {
        final TableColumn<TableViewViewModel, String> column = new TableColumn<>(header);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        table.getColumns().add(column);
    }
}
