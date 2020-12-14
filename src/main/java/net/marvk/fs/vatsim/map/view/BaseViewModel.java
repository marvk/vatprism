package net.marvk.fs.vatsim.map.view;

import de.saxsys.mvvmfx.ViewModel;
import net.marvk.fs.vatsim.map.data.Data;

public class BaseViewModel implements ViewModel {
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

    public void switchToMapTab() {
        Notifications.SWITCH_TO_TAB.publish(0);
    }
}
