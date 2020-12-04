package net.marvk.fs.vatsim.map.view.map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Data;

import java.util.ArrayList;

public class ContextMenuItems<E extends Data> {
    private final String label;
    private final ObservableList<E> items;

    public ContextMenuItems(final String label) {
        this(label, FXCollections.observableArrayList());
    }

    public ContextMenuItems(final ContextMenuItems<E> contextMenuItems) {
        this(contextMenuItems.label, FXCollections.observableList(new ArrayList<>(contextMenuItems.items)));
    }

    private ContextMenuItems(final String label, final ObservableList<E> items) {
        this.label = label;
        this.items = items;
    }

    public String getLabel() {
        return label;
    }

    public ObservableList<E> getItems() {
        return items;
    }
}
