package net.marvk.fs.vatsim.map.view.filter.filteroutline;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.view.filter.FilterListViewModel;

public class FilterOutlineView implements FxmlView<FilterOutlineViewModel> {
    @FXML
    private Button deleteButton;
    @FXML
    private ListView<FilterListViewModel> list;

    @InjectViewModel
    private FilterOutlineViewModel viewModel;

    public void initialize() {
        list.setItems(viewModel.getFilters());
        list.setCellFactory(param -> new FilterListViewModelListCell());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        deleteButton.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
    }

    @FXML
    private void add() {
        viewModel.addNewFilter();
        list.getSelectionModel().select(list.getItems().size() - 1);
    }

    @FXML
    private void delete() {
        viewModel.delete(list.getSelectionModel().getSelectedItem());
        viewModel.setActive(list.getSelectionModel().getSelectedItem());
    }

    private class FilterListViewModelListCell extends ListCell<FilterListViewModel> {
        private final Label label = new Label();
        private final CheckBox checkBox = new CheckBox();
        private final Region region = new Region();
        private final HBox hBox = new HBox(label, region, checkBox);

        FilterListViewModelListCell() {
            getStyleClass().add("vatprism-cell");

            onMouseClickedProperty().bind(itemProperty().map(filterListViewModel -> (EventHandler<MouseEvent>) event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    viewModel.setActive(filterListViewModel);
                    event.consume();
                }
            }));
            itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    checkBox.selectedProperty().unbind();
                    label.textProperty().unbind();
                    label.setText(null);
                } else {
                    // todo checkbox
                    //                    checkBox.selectedProperty().bind(newValue.filterProperty().map(Filter::isEnabled));
                    label.textProperty().bind(newValue.filterProperty().map(Filter::getName));
                }
            });

            checkBox.getStyleClass().add("check-box-standalone");
            graphicProperty().bind(itemProperty().map(e -> hBox));
            HBox.setHgrow(region, Priority.ALWAYS);
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPrefHeight(20);
            hBox.setPadding(new Insets(0, 3, 0, 5));
        }
    }
}
