package net.marvk.fs.vatsim.map.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

public class TableNoneSelectionModel<T> extends TableView.TableViewSelectionModel<T> {
    public TableNoneSelectionModel(final TableView<T> tableView) {
        super(tableView);
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public void selectIndices(final int index, final int... indices) {
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void selectFirst() {
    }

    @Override
    public void selectLast() {
    }

    @Override
    public void clearAndSelect(final int index) {
    }

    @Override
    public void select(final int index) {
    }

    @Override
    public void select(final T obj) {
    }

    @Override
    public void clearSelection(final int index) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public boolean isSelected(final int index) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void selectPrevious() {
    }

    @Override
    public void selectNext() {
    }

    @Override
    public ObservableList<TablePosition> getSelectedCells() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public boolean isSelected(final int row, final TableColumn<T, ?> column) {
        return false;
    }

    @Override
    public void select(final int row, final TableColumn<T, ?> column) {
    }

    @Override
    public void clearAndSelect(final int row, final TableColumn<T, ?> column) {
    }

    @Override
    public void clearSelection(final int row, final TableColumn<T, ?> column) {
    }

    @Override
    public void selectLeftCell() {
    }

    @Override
    public void selectRightCell() {
    }

    @Override
    public void selectAboveCell() {
    }

    @Override
    public void selectBelowCell() {
    }
}
