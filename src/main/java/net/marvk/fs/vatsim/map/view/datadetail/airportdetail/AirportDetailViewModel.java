package net.marvk.fs.vatsim.map.view.datadetail.airportdetail;

import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

public class AirportDetailViewModel extends DataDetailSubViewModel<Airport> {
    public void setToFir() {
        if (getData() != null) {
            setDataDetail(getData().getFlightInformationRegionBoundary());
        }
    }
}
