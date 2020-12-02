package net.marvk.fs.vatsim.map.view.toolbar;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;

public class ToolBarViewModel implements ViewModel {
    private final NotificationCenter notificationCenter;

    @Inject
    public ToolBarViewModel(final NotificationCenter notificationCenter) {
        this.notificationCenter = notificationCenter;

    }

    public void refresh() {
        notificationCenter.publish("RELOAD_CLIENTS");
    }
}
