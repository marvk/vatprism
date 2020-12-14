package net.marvk.fs.vatsim.map.view.datadetail.detailsubview;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.marvk.fs.vatsim.map.view.BaseViewModel;

public class DetailSubViewModel<D> extends BaseViewModel {
    protected final ObjectProperty<D> data = new SimpleObjectProperty<>();

    public D getData() {
        return data.get();
    }

    public ObjectProperty<D> dataProperty() {
        return data;
    }

    public void setData(final D data) {
        this.data.set(data);
    }
}
