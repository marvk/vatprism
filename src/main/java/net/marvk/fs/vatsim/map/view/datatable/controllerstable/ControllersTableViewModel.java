package net.marvk.fs.vatsim.map.view.datatable.controllerstable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class ControllersTableViewModel extends SimpleTableViewModel<Controller> {
    @Inject
    public ControllersTableViewModel(final Preferences preferences) {
        super(preferences);
    }

    @Override
    public ObservableList<Controller> items() {
        return toolbarScope.filteredControllers();
    }
}
