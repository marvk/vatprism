package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.marvk.fs.vatsim.api.data.VatsimAtis;
import net.marvk.fs.vatsim.api.data.VatsimClient;

public class Atis extends Controller {
    private final StringProperty atisCode = new SimpleStringProperty();

    @Override
    public void setFromModel(final VatsimClient client) {
        final VatsimAtis atis = (VatsimAtis) client;
        super.setFromModel(atis);
        atisCode.set(atis.getAtisCode());
    }

    @Override
    public ClientType clientType() {
        return ClientType.ATIS;
    }

    public String getAtisCode() {
        return atisCode.get();
    }

    public ReadOnlyStringProperty atisCodeProperty() {
        return atisCode;
    }
}
