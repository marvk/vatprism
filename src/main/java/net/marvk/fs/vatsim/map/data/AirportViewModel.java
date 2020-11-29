package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimAirport;
import net.marvk.fs.vatsim.map.GeomUtil;

public class AirportViewModel extends SimpleDataViewModel<VatsimAirport, AirportViewModel> {

    private Point2D position;

    public AirportViewModel() {
        super();
        modelProperty().addListener((observable, oldValue, newValue) -> {
            position = GeomUtil.parsePoint(newValue.getPosition().getX(), newValue.getPosition().getY());
        });
    }

    public StringProperty icaoProperty() {
        return stringProperty("icao", VatsimAirport::getIcao);
    }

    public StringProperty nameProperty() {
        return stringProperty("fir", VatsimAirport::getName);
    }

    public ObjectProperty<Point2D> positionProperty() {
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

    public Point2D getPosition() {
        return position;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AirportViewModel that = (AirportViewModel) o;

        return getModel() != null ? getModel().equals(that.getModel()) : that.getModel() == null;
    }

    @Override
    public int hashCode() {
        return getModel() != null ? getModel().hashCode() : 0;
    }
}
