package net.marvk.fs.vatsim.map.view.datatable.airportstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.AirportRepository;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class AirportsTableViewModel extends SimpleTableViewModel<Airport> {
    @Inject
    public AirportsTableViewModel(final AirportRepository repository) {
        super(repository);
    }
}
