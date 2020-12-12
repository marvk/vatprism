package net.marvk.fs.vatsim.map.view;

import com.google.inject.Singleton;
import de.saxsys.mvvmfx.Scope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.view.painter.PainterExecutor;

@Singleton
public class SettingsScope implements Scope {
    private final ObservableList<PainterExecutor<?>> painters = FXCollections.observableArrayList();

    public ObservableList<PainterExecutor<?>> getPainters() {
        return painters;
    }
}
