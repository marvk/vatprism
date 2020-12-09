package net.marvk.fs.vatsim.map.view.datadetail.controllersdetail;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubViewModel;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

public class ControllersDetailViewModel extends DetailSubViewModel<ObservableList<Controller>> {
    private final Preferences preferences;

    @Inject
    public ControllersDetailViewModel(final Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
