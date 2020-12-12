package net.marvk.fs.vatsim.map.view;

import com.google.inject.Singleton;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@Singleton
public class ToolbarScope implements Scope {
    private final BooleanProperty autoReload = new SimpleBooleanProperty();

    public boolean isAutoReload() {
        return autoReload.get();
    }

    public BooleanProperty autoReloadProperty() {
        return autoReload;
    }

    public void setAutoReload(final boolean autoReload) {
        this.autoReload.set(autoReload);
    }
}
