package net.marvk.fs.vatsim.map.view.toolbar;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;

public class ToolBarViewModel implements ViewModel {
    private final NotificationCenter notificationCenter;

    @InjectScope
    private SettingsScope settingsScope;

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public ToolBarViewModel(final NotificationCenter notificationCenter) {
        this.notificationCenter = notificationCenter;
    }

    public void refresh() {
        notificationCenter.publish("RELOAD_CLIENTS");
    }

    public void setAutoReload(final boolean autoReload) {
        toolbarScope.setAutoReload(autoReload);
    }

    public ObservableList<PainterExecutor<?>> getPainterExecutors() {
        return settingsScope.getPainters();
    }

    public void triggerRepaint() {
        notificationCenter.publish("REPAINT");
    }
}
