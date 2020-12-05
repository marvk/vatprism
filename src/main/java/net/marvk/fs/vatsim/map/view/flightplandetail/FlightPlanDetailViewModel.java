package net.marvk.fs.vatsim.map.view.flightplandetail;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.utils.notifications.NotificationCenter;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightPlan;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;

import java.util.function.Supplier;

public class FlightPlanDetailViewModel extends DataDetailSubViewModel<FlightPlan> {
    @Inject
    public FlightPlanDetailViewModel(final NotificationCenter notificationCenter) {
        super(notificationCenter, null);
    }

    public void goToDeparture() {
        setDataDetailAirport(() -> getData().getDepartureAirport());
    }

    public void goToArrival() {
        setDataDetailAirport(() -> getData().getArrivalAirport());
    }

    private void setDataDetailAirport(final Supplier<Airport> airportSupplier) {
        if (getData() != null) {
            final Airport airport = airportSupplier.get();
            if (airport != null) {
                notificationCenter.publish("SET_DATA_DETAIL", airport);
            }
        }
    }
}
