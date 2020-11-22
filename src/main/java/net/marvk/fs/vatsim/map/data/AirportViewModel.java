package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import net.marvk.fs.vatsim.api.data.VatsimAirport;

public class AirportViewModel extends SimpleDataViewModel<VatsimAirport, AirportViewModel> {
    public AirportViewModel(final AirportViewModel viewModel) {
        super(viewModel);
    }

    public AirportViewModel(final VatsimAirport vatsimAirport) {
        super(vatsimAirport);
    }

    public AirportViewModel() {
        super();
    }

    public StringProperty icaoProperty() {
        return stringProperty("icao", VatsimAirport::getIcao);
    }

    public StringProperty nameProperty() {
        return stringProperty("fir", VatsimAirport::getName);
    }

    public ObjectProperty<Point> positionProperty() {
        return pointProperty("position", VatsimAirport::getPosition);
    }

    public StringProperty flightInformationRegionProperty() {
        return stringProperty("fir", VatsimAirport::getFlightInformationRegion);
    }

    public StringProperty iataLidProperty() {
        return stringProperty("fir", VatsimAirport::getIataLid);
    }

    public BooleanProperty pseudoProperty() {
        return booleanProperty("pseudo", VatsimAirport::getPseudo);
    }
}
