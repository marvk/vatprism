package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.GeomUtil;

public class ClientStatusViewModel extends SimpleDataViewModel<VatsimClient, ClientStatusViewModel> implements ViewModel {
    public ObjectProperty<Point2D> position() {
        return objectProperty("position", c -> GeomUtil.parsePoint(c.getLongitude(), c.getLatitude()));
    }

    public DoubleProperty heading() {
        return doubleProperty("heading", c -> ParseUtil.parseNullSafe(c.getHeading(), Double::parseDouble));
    }

    public DoubleProperty groundSpeed() {
        return doubleProperty("groundSpeed", c -> ParseUtil.parseNullSafe(c.getGroundSpeed(), Double::parseDouble));
    }

    public DoubleProperty altitude() {
        return doubleProperty("altitude", c -> ParseUtil.parseNullSafe(c.getAltitude(), Double::parseDouble));
    }
}
