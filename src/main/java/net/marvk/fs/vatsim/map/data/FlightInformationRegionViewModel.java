package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.ViewModel;
import net.marvk.fs.vatsim.api.data.VatsimFlightInformationRegion;

public class FlightInformationRegionViewModel extends SimpleDataViewModel<VatsimFlightInformationRegion, FlightInformationRegionViewModel> implements ViewModel {
    public FlightInformationRegionViewModel(final FlightInformationRegionViewModel viewModel) {
        super(viewModel);
    }

    public FlightInformationRegionViewModel(final VatsimFlightInformationRegion vatsimFlightInformationRegion) {
        super(vatsimFlightInformationRegion);
    }

    public FlightInformationRegionViewModel() {
        super();
    }
}
