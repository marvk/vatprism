package net.marvk.fs.vatsim.map.view.toolbar;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.ToolbarScope;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

public class ToolBarViewModel implements ViewModel {

    private final Preferences preferences;
    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public ToolBarViewModel(final Preferences preferences) {
        this.preferences = preferences;
    }

    public void refresh() {
        Notifications.RELOAD_CLIENTS.publish();
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setAutoReload(final boolean autoReload) {
        toolbarScope.setAutoReload(autoReload);
    }

    public void triggerRepaint() {
        Notifications.REPAINT.publish();
    }
}
