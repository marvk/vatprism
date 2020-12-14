package net.marvk.fs.vatsim.map.view.table;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextFlow;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.view.TextFlowHighlighter;
import org.controlsfx.control.table.TableFilter;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractTableView<ViewModel extends SimpleTableViewModel<Model>, Model extends Data> implements FxmlView<ViewModel> {
    private final TextFlowHighlighter textFlowHighlighter;
    @FXML
    protected TableView<Model> table;

    @InjectViewModel
    protected ViewModel viewModel;

    @Inject
    public AbstractTableView(final TextFlowHighlighter textFlowHighlighter) {
        this.textFlowHighlighter = textFlowHighlighter;
    }

    public void initialize() {
        table.setFixedCellSize(18);

        final SortedList<Model> value = new SortedList<>(viewModel.items());
        value.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(value);

        table.setRowFactory(param -> new DataTableRow());

        final var selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        viewModel.selectedItemProperty().bind(selectionModel.selectedItemProperty());
    }

    protected void enableFilter() {
        TableFilter.forTableView(table).lazy(true).apply();
    }

    public void addColumnWithStringFactory(
            final String header,
            final Function<Model, ObservableValue<String>> valueFactory,
            final boolean mono
    ) {
        addColumnWithStringFactory(header, (model, ignored) -> valueFactory.apply(model), mono);
    }

    public void addColumnWithStringFactory(
            final String header,
            final BiFunction<Model, String, ObservableValue<String>> valueFactory,
            final boolean mono
    ) {
        addColumnWithPaneFactory(
                header,
                valueFactory,
                (cellValue, query) -> textFlow(cellValue, mono)
        );
    }

    private TextFlow textFlow(final String cellValue, final boolean mono) {
        if (viewModel.getQuery() == null || viewModel.getQuery().isBlank()) {
            return new TextFlow(textFlowHighlighter.createSimpleTextFlow(cellValue, mono));
        }

        return new TextFlow(textFlowHighlighter.createHighlightedTextFlow(cellValue, viewModel.getPattern(), mono));
    }

    public <CellValue> void addColumnWithPaneFactory(
            final String header,
            final BiFunction<Model, String, ObservableValue<CellValue>> valueFactory,
            final BiFunction<CellValue, String, Pane> paneSupplier
    ) {
        final TableColumn<Model, CellValue> column = new TableColumn<>(header);
        column.setCellValueFactory(param -> valueFactory.apply(param.getValue(), viewModel.getQuery()));
        column.setCellFactory(param -> new DataTableCell<>(paneSupplier));
        column.setSortable(true);
        table.getColumns().add(column);
    }

    private class DataTableRow extends TableRow<Model> {
        public DataTableRow() {
            setOnMouseClicked(e -> {
                viewModel.setDataDetail(getItem());
                viewModel.goTo(getItem());
                viewModel.switchToMapTab();
            });
        }
    }

    private class DataTableCell<CellValue> extends TableCell<Model, CellValue> {
        private final BiFunction<CellValue, String, Pane> paneSupplier;

        public DataTableCell(final BiFunction<CellValue, String, Pane> paneSupplier) {
            this.paneSupplier = paneSupplier;
        }

        @Override
        protected void updateItem(final CellValue item, final boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(paneSupplier.apply(item, viewModel.getQuery()));
            }
        }
    }
}
