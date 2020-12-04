package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.GeomUtil;

import java.util.Objects;

public abstract class Client implements Settable<VatsimClient>, Data {
    private final StringProperty cid = new SimpleStringProperty();
    private final StringProperty callsign = new SimpleStringProperty();
    private final StringProperty realName = new SimpleStringProperty();
    private final StringProperty server = new SimpleStringProperty();

    private final ObjectProperty<Point2D> position = new SimpleObjectProperty<>();

    @Override
    public void setFromModel(final VatsimClient model) {
        Objects.requireNonNull(model);

        cid.set(model.getCid());
        callsign.set(model.getCallsign());
        realName.set(model.getRealName());
        server.set(model.getRealName());
        position.set(GeomUtil.parsePoint(model.getLongitude(), model.getLatitude()));
    }

    public String getCid() {
        return cid.get();
    }

    public ReadOnlyStringProperty cidProperty() {
        return cid;
    }

    public String getCallsign() {
        return callsign.get();
    }

    public ReadOnlyStringProperty callsignProperty() {
        return callsign;
    }

    public String getRealName() {
        return realName.get();
    }

    public ReadOnlyStringProperty realNameProperty() {
        return realName;
    }

    public String getServer() {
        return server.get();
    }

    public ReadOnlyStringProperty serverProperty() {
        return server;
    }

    public Point2D getPosition() {
        return position.get();
    }

    public ReadOnlyObjectProperty<Point2D> positionProperty() {
        return position;
    }
}
