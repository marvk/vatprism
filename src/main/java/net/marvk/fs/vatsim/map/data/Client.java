package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import net.marvk.fs.vatsim.api.data.VatsimClient;

import java.time.ZonedDateTime;
import java.util.Objects;

public abstract class Client implements Settable<VatsimClient>, Data {
    private final StringProperty cid = new SimpleStringProperty();
    private final StringProperty callsign = new SimpleStringProperty();
    private final StringProperty realName = new SimpleStringProperty();
    private final StringProperty server = new SimpleStringProperty();

    private final ObjectProperty<ZonedDateTime> logonTime = new SimpleObjectProperty<>();
    private final ObjectProperty<ZonedDateTime> lastUpdatedTime = new SimpleObjectProperty<>();

    @Override
    public void setFromModel(final VatsimClient client) {
        Objects.requireNonNull(client);

        cid.set(client.getCid());
        callsign.set(client.getCallsign());
        realName.set(client.getName());
        server.set(client.getServer().getName());
        logonTime.set(client.getLogon());
        lastUpdatedTime.set(client.getLastUpdate());
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

    public ZonedDateTime getLogonTime() {
        return logonTime.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> logonTimeProperty() {
        return logonTime;
    }

    public ZonedDateTime getLastUpdatedTime() {
        return lastUpdatedTime.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> lastUpdatedTimeProperty() {
        return lastUpdatedTime;
    }
}
