package net.marvk.fs.vatsim.map.view.airports;

import net.marvk.fs.vatsim.map.data.AirportViewModel;
import net.marvk.fs.vatsim.map.repository.AirportsRepository;
import net.marvk.fs.vatsim.map.view.table.SimpleTableViewModel;

public class AirportsViewModel extends SimpleTableViewModel<AirportViewModel> {
    public AirportsViewModel(final AirportsRepository airportsRepository) {
        super(airportsRepository);
    }
}
