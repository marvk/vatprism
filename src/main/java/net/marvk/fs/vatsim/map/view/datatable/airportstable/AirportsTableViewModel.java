package net.marvk.fs.vatsim.map.view.datatable.airportstable;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class AirportsTableViewModel extends SimpleTableViewModel<Airport> {
    @Override
    public ObservableList<Airport> items() {
        return toolbarScope.filteredAirports();
    }
}
