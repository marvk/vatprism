package net.marvk.fs.vatsim.map.view.airports;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.AirportViewModel;
import net.marvk.fs.vatsim.map.repository.AirportRepository;
import net.marvk.fs.vatsim.map.view.table.SimpleTableViewModel;

public class AirportsViewModel extends SimpleTableViewModel<AirportViewModel> {
    @Inject
    public AirportsViewModel(final AirportRepository airportRepository) {
        super(airportRepository);
    }
}
