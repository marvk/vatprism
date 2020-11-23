package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ObjectProperty;

public interface DataViewModel<Model, ViewModel extends DataViewModel<Model, ViewModel>> {
    Model getModel();

    void setModel(final Model model);

    void setModelFromViewModel(final ViewModel viewModel);

    ObjectProperty<Model> modelProperty();

    void clear();
}
