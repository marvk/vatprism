package net.marvk.fs.vatsim.map.view.datadetail.controllerdetail;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubViewModel;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

public class ControllerDetailViewModel extends DataDetailSubViewModel<Controller> {
    private final Preferences preferences;

    @Inject
    public ControllerDetailViewModel(final Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
