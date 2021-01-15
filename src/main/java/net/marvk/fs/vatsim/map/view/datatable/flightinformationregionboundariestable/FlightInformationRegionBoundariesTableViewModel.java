package net.marvk.fs.vatsim.map.view.datatable.flightinformationregionboundariestable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class FlightInformationRegionBoundariesTableViewModel extends SimpleTableViewModel<FlightInformationRegionBoundary> {
    @Inject
    public FlightInformationRegionBoundariesTableViewModel(final Preferences preferences) {
        super(preferences);
    }

    @Override
    public ObservableList<FlightInformationRegionBoundary> items() {
        return toolbarScope.filteredFirbs();
    }
}
