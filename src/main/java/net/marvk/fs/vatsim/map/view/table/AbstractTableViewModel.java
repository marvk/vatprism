package net.marvk.fs.vatsim.map.view.table;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;

public abstract class AbstractTableViewModel<TableViewViewModel> implements ViewModel {
    private final ObjectProperty<TableViewViewModel> selectedItem = new SimpleObjectProperty<>();

    public abstract ObservableList<TableViewViewModel> items();

    public TableViewViewModel getSelectedItem() {
        return selectedItem.get();
    }

    public ObjectProperty<TableViewViewModel> selectedItemProperty() {
        return selectedItem;
    }

    public void setSelectedItem(final TableViewViewModel selectedItem) {
        this.selectedItem.set(selectedItem);
    }
}
