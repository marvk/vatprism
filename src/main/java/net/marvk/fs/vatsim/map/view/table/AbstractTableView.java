package net.marvk.fs.vatsim.map.view.table;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
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
        this.addColumnFromPropertyExtractor(header, new PropertyValueFactory<>(propertyName));
    }

    protected void addColumn(final String header, final Callback<TableViewViewModel, ObservableValue<String>> callback) {
        this.addColumnFromPropertyExtractor(header, param -> callback.call(param.getValue()));
    }

    protected void addColumnFromPropertyExtractor(final String header, final Callback<TableColumn.CellDataFeatures<TableViewViewModel, String>, ObservableValue<String>> callback) {
        final TableColumn<TableViewViewModel, String> column = new TableColumn<>(header);
        column.setCellValueFactory(callback);
        table.getColumns().add(column);
    }
}
