package net.marvk.fs.vatsim.map.view.datatable.controllerstable;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class ControllersTableViewModel extends SimpleTableViewModel<Controller> {
    @Override
    public ObservableList<Controller> items() {
        return toolbarScope.filteredControllers();
    }
}
