package net.marvk.fs.vatsim.map.view;

import com.google.inject.Singleton;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

@Singleton
public class ToolbarScope implements Scope {
    private final BooleanProperty autoReload = new SimpleBooleanProperty();

    private final BooleanProperty reloadExecutable = new SimpleBooleanProperty();

    private final BooleanProperty reloadRunning = new SimpleBooleanProperty();

    private final ObjectProperty<Throwable> reloadException = new SimpleObjectProperty<>();

    public boolean isAutoReload() {
        return autoReload.get();
    }

    public BooleanProperty autoReloadProperty() {
        return autoReload;
    }

    public void setAutoReload(final boolean autoReload) {
        this.autoReload.set(autoReload);
    }

    public boolean isReloadExecutable() {
        return reloadExecutable.get();
    }

    public BooleanProperty reloadExecutableProperty() {
        return reloadExecutable;
    }

    public void setReloadExecutable(final boolean reloadExecutable) {
        this.reloadExecutable.set(reloadExecutable);
    }

    public boolean isReloadRunning() {
        return reloadRunning.get();
    }

    public BooleanProperty reloadRunningProperty() {
        return reloadRunning;
    }

    public void setReloadRunning(final boolean reloadRunning) {
        this.reloadRunning.set(reloadRunning);
    }

    public Throwable getReloadException() {
        return reloadException.get();
    }

    public ObjectProperty<Throwable> reloadExceptionProperty() {
        return reloadException;
    }

    public void setReloadException(final Throwable reloadException) {
        this.reloadException.set(reloadException);
    }
}
