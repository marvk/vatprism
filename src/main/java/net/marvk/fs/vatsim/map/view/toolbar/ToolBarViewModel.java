package net.marvk.fs.vatsim.map.view.toolbar;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.RepositoryException;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.ToolbarScope;

public class ToolBarViewModel implements ViewModel {
    private final ReadOnlyStringWrapper errorMessage = new ReadOnlyStringWrapper();
    private final BooleanProperty autoReload = new SimpleBooleanProperty();

    private final HostServices hostServices;
    private final Preferences preferences;

    @InjectScope
    private ToolbarScope toolbarScope;

    @Inject
    public ToolBarViewModel(final HostServices hostServices, final Preferences preferences) {
        this.hostServices = hostServices;
        this.preferences = preferences;
    }

    public void initialize() {
        errorMessage.bind(Bindings.createStringBinding(
                () -> resolveException(toolbarScope.getReloadException()),
                toolbarScope.reloadExceptionProperty()
        ));
        toolbarScope.autoReloadProperty().bindBidirectional(autoReload);
        toolbarScope.autoReloadProperty().bindBidirectional(preferences.booleanProperty("general.auto_reload"));
    }

    private String resolveException(final Throwable reloadException) {
        if (reloadException == null) {
            return null;
        }

        if (reloadException instanceof RepositoryException) {
            return "Repository Exception";
        }

        return "Unknown exception " + reloadException.getClass().getSimpleName();
    }

    public void refresh() {
        Notifications.RELOAD_CLIENTS.publish();
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void triggerRepaint() {
        Notifications.REPAINT.publish();
    }

    public boolean isReloadExecutable() {
        return toolbarScope.isReloadExecutable();
    }

    public ReadOnlyBooleanProperty reloadExecutableProperty() {
        return toolbarScope.reloadExecutableProperty();
    }

    public boolean isReloadRunning() {
        return toolbarScope.isReloadRunning();
    }

    public ReadOnlyBooleanProperty reloadRunningProperty() {
        return toolbarScope.reloadRunningProperty();
    }

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public ReadOnlyStringProperty errorMessageProperty() {
        return errorMessage.getReadOnlyProperty();
    }

    public boolean isAutoReload() {
        return autoReload.get();
    }

    public BooleanProperty autoReloadProperty() {
        return autoReload;
    }

    public void setAutoReload(final boolean autoReload) {
        this.autoReload.set(autoReload);
    }

    public void visitDiscord() {
        hostServices.showDocument("https://discord.gg/XPpFHhT8sk");
    }

    public void visitGithub() {
        hostServices.showDocument("https://github.com/marvk/vatprism");
    }
}
