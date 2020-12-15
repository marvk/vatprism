package net.marvk.fs.vatsim.map.view.datatable;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;

public abstract class AbstractTableView<ViewModel extends SimpleTableViewModel<Model>, Model extends Data> extends TableView<Model> implements JavaView<ViewModel> {
    private final TextFlowHighlighter textFlowHighlighter;
    private ColumnBuilderFactory<Model> columnBuilder;
    @FXML
    protected TableView<Model> table;

    @InjectViewModel
    protected ViewModel viewModel;

    @Inject
    public AbstractTableView(final TextFlowHighlighter textFlowHighlighter) {
        this.table = this;
        this.textFlowHighlighter = textFlowHighlighter;
    }

    protected <CellValue> ColumnBuilderFactory.TitleStep<Model, CellValue> newColumnBuilder() {
        return columnBuilder.newBuilder();
    }

    public void initialize() {
        columnBuilder = new ColumnBuilderFactory<>(viewModel, textFlowHighlighter, c -> table.getColumns().add(c));
//        table.setFixedCellSize(18);

        getStyleClass().add("clickable-rows");

        final SortedList<Model> value = new SortedList<>(viewModel.items());
        value.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(value);

        table.setRowFactory(param -> new DataTableRow());

        final var selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        viewModel.selectedItemProperty().bind(selectionModel.selectedItemProperty());

        initializeColumns();
    }

    protected abstract void initializeColumns();

    private class DataTableRow extends TableRow<Model> {
        public DataTableRow() {
            setOnMouseClicked(this::handleMouseClicked);
        }

        private void handleMouseClicked(final javafx.scene.input.MouseEvent e) {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (getItem() != null) {
                    viewModel.setDataDetail(getItem());
                    viewModel.goTo(getItem());
                    viewModel.switchToMapTab();
                }
            }
        }
    }
}
