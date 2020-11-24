package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import net.marvk.fs.vatsim.api.data.VatsimClient;

public class ClientStatusViewModel extends SimpleDataViewModel<VatsimClient, ClientStatusViewModel> implements ViewModel {
    public ObjectProperty<Point> position() {
        return objectProperty("position", c -> Point.from(c.getLongitude(), c.getLatitude()));
    }

    public DoubleProperty heading() {
        return doubleProperty("heading", c -> ParseUtil.parseNullSafe(c.getHeading(), Double::parseDouble));
    }

    public DoubleProperty groundSpeed() {
        return doubleProperty("groundSpeed", c -> ParseUtil.parseNullSafe(c.getGroundSpeed(), Double::parseDouble));
    }
}
