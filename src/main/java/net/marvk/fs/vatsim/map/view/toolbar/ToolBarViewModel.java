package net.marvk.fs.vatsim.map.view.toolbar;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.SettingsScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;

public class ToolBarViewModel implements ViewModel {
    @InjectScope
    private SettingsScope settingsScope;

    @InjectScope
    private ToolbarScope toolbarScope;

    public void refresh() {
        Notifications.RELOAD_CLIENTS.publish();
    }

    public void setAutoReload(final boolean autoReload) {
        toolbarScope.setAutoReload(autoReload);
    }

    public ObservableList<PainterExecutor<?>> getPainterExecutors() {
        return settingsScope.getPainters();
    }

    public void triggerRepaint() {
        Notifications.REPAINT.publish();
    }
}
