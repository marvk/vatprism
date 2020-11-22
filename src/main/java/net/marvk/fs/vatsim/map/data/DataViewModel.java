package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ObjectProperty;

public interface DataViewModel<Model> {
    Model getModel();

    void setModel(final Model model);

    ObjectProperty<Model> modelProperty();

    void clear();
}
