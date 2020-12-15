package net.marvk.fs.vatsim.map.view.datatable.upperinformationregionstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.data.UpperInformationRegionRepository;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class UpperInformationRegionsTableViewModel extends SimpleTableViewModel<UpperInformationRegion> {
    @Inject
    public UpperInformationRegionsTableViewModel(final UpperInformationRegionRepository repository) {
        super(repository);
    }
}
