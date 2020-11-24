package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.StringProperty;
import net.marvk.fs.vatsim.api.data.VatsimFlightInformationRegion;

public class FlightInformationRegionViewModel extends SimpleDataViewModel<VatsimFlightInformationRegion, FlightInformationRegionViewModel> implements ViewModel {
    public StringProperty icaoProperty() {
        return stringProperty("icao", VatsimFlightInformationRegion::getIcao);
    }

    public StringProperty nameProperty() {
        return stringProperty("name", VatsimFlightInformationRegion::getName);
    }

    public StringProperty prefixPositionProperty() {
        return stringProperty("prefixPosition", VatsimFlightInformationRegion::getPrefixPosition);
    }

    public StringProperty unknown1Property() {
        return stringProperty("unknown1", VatsimFlightInformationRegion::getUnknown1);
    }
}
