package net.marvk.fs.vatsim.map.view.flightplandetail;

import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;

import java.util.function.Supplier;

public class FlightPlanDetailViewModel extends DataDetailSubViewModel<FlightPlan> {
    public void setToArrival() {
        setDataDetailAirport(() -> getData().getDepartureAirport());
    }

    public void setToDeparture() {
        setDataDetailAirport(() -> getData().getArrivalAirport());
    }

    private void setDataDetailAirport(final Supplier<Airport> airportSupplier) {
        if (getData() != null) {
            setDataDetail(airportSupplier.get());
        }
    }
}
