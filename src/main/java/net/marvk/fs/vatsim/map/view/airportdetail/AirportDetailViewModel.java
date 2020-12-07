package net.marvk.fs.vatsim.map.view.airportdetail;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

public class AirportDetailViewModel extends DataDetailSubViewModel<Airport> {
    private final Preferences preferences;

    @Inject
    public AirportDetailViewModel(final Preferences preferences) {
        this.preferences = preferences;
    }

    public void setToFir() {
        if (getData() != null) {
            setDataDetail(getData().getFlightInformationRegionBoundary());
        }
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
