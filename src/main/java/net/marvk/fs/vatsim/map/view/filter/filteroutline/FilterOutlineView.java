package net.marvk.fs.vatsim.map.view.filter.filteroutline;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.view.filter.FilterListViewModel;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

public class FilterOutlineView implements FxmlView<FilterOutlineViewModel> {
    @FXML
    private TableView<FilterListViewModel> table;

    @InjectViewModel
    private FilterOutlineViewModel viewModel;

    public void initialize() {
        table.setItems(viewModel.getFilters());
        final TableColumn<FilterListViewModel, FilterListViewModel> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("self"));
        nameColumn.setCellFactory(param -> new NameCell());
        nameColumn.setPrefWidth(200);
        nameColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        final TableColumn<FilterListViewModel, FilterListViewModel> deleteColumn = new TableColumn<>();
        deleteColumn.setPrefWidth(24);
        deleteColumn.setCellValueFactory(new PropertyValueFactory<>("self"));
        deleteColumn.setCellFactory(param -> new RemoveButtonCell());
        table.getColumns().add(nameColumn);
        table.getColumns().add(deleteColumn);

        table.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                viewModel.setActive(table.getSelectionModel().getSelectedItem());
            }
        });
    }

    @FXML
    private void add() {
        viewModel.addNewFilter();
    }

    private static class NameCell extends TableCell<FilterListViewModel, FilterListViewModel> {
        public NameCell() {
            getStyleClass().add("center-left-cell");
        }

        @Override
        protected void updateItem(final FilterListViewModel item, final boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                textProperty().unbind();
                setText(null);
                styleProperty().unbind();
                setStyle("-fx-background-color: transparent;");
            } else {
                styleProperty().bind(Bindings.createStringBinding(
                        () -> backgroundString(item),
                        item.filterProperty()
                ));
                textProperty().bind(item.nameProperty());
            }
        }

        private static String backgroundString(final FilterListViewModel item) {
            final String backgroundHex = hex(item.getFilter().getBackgroundColor());
            final String textHex = hex(item.getFilter().getTextColor());

            return "-fx-background-color: #%s; -fx-text-fill: #%s;".formatted(backgroundHex, textHex);
        }

        private static String hex(final Color color) {
            return color.toString().substring(2, 10);
        }
    }

    private class RemoveButtonCell extends TableCell<FilterListViewModel, FilterListViewModel> {
        private Button button;

        private Button getButton(final FilterListViewModel viewModel) {
            if (button == null) {
                button = new Button();
                button.setGraphic(new FontIcon(Octicons.TRASHCAN_16));
                button.getStyleClass().add("icon-button");
            }
            button.setOnAction(e -> FilterOutlineView.this.viewModel.delete(viewModel));
            return button;
        }

        @Override
        protected void updateItem(final FilterListViewModel item, final boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setGraphic(null);
            } else {
                setGraphic(getButton(item));
            }
        }
    }
}
