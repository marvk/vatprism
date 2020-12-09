package net.marvk.fs.vatsim.map.view.datadetail.detailsubview;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.view.Notifications;

public class DetailSubViewModel<D> implements ViewModel {
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

    public void goTo(final Data data) {
        if (data != null) {
            Notifications.PAN_TO_DATA.publish(data);
        }
    }

    public void setDataDetail(final Data data) {
        if (data != null) {
            Notifications.SET_DATA_DETAIL.publish(data);
        }
    }
}
