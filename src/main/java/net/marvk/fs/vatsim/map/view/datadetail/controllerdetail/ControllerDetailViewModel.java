package net.marvk.fs.vatsim.map.view.datadetail.controllerdetail;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

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
