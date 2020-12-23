package net.marvk.fs.vatsim.map.view.datatable.flightinformationregionboundariestable;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class FlightInformationRegionBoundariesTableViewModel extends SimpleTableViewModel<FlightInformationRegionBoundary> {
    @Override
    public ObservableList<FlightInformationRegionBoundary> items() {
        return toolbarScope.filteredFirbs();
    }
}
