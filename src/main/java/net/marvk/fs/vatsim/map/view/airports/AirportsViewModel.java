package net.marvk.fs.vatsim.map.view.airports;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.AirportRepository;
import net.marvk.fs.vatsim.map.view.table.SimpleTableViewModel;

public class AirportsViewModel extends SimpleTableViewModel<Airport> {
    @Inject
    public AirportsViewModel(final AirportRepository airportRepository) {
        super(airportRepository);
    }

    @Override
    protected boolean matchesQuery(final Airport e) {
        return false;
    }
}
