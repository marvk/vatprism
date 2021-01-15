package net.marvk.fs.vatsim.map.view.datatable.airportstable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class AirportsTableViewModel extends SimpleTableViewModel<Airport> {
    @Inject
    public AirportsTableViewModel(final Preferences preferences) {
        super(preferences);
    }

    @Override
    public ObservableList<Airport> items() {
        return toolbarScope.filteredAirports();
    }
}
