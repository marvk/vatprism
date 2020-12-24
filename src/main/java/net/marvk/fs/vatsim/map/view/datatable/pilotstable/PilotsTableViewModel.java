package net.marvk.fs.vatsim.map.view.datatable.pilotstable;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Pilot;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class PilotsTableViewModel extends SimpleTableViewModel<Pilot> {
    @Override
    public ObservableList<Pilot> items() {
        return toolbarScope.filteredPilots();
    }
}
