package net.marvk.fs.vatsim.map.view.datatable.flightinformationregionboundariestable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundaryRepository;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class FlightInformationRegionBoundariesTableViewModel extends SimpleTableViewModel<FlightInformationRegionBoundary> {
    @Inject
    public FlightInformationRegionBoundariesTableViewModel(final FlightInformationRegionBoundaryRepository repository) {
        super(repository);
    }
}
