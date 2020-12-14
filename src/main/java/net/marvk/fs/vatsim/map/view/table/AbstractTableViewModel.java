package net.marvk.fs.vatsim.map.view.table;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.view.BaseViewModel;

public abstract class AbstractTableViewModel<Model> extends BaseViewModel {
    private final ObjectProperty<Model> selectedItem = new SimpleObjectProperty<>();

    public abstract ObservableList<Model> items();

    public Model getSelectedItem() {
        return selectedItem.get();
    }

    public ObjectProperty<Model> selectedItemProperty() {
        return selectedItem;
    }

    public void setSelectedItem(final Model selectedItem) {
        this.selectedItem.set(selectedItem);
    }
}
