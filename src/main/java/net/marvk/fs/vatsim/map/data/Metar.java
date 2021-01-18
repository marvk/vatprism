package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

import java.time.LocalDateTime;

public class Metar {
    private final ReadOnlyObjectProperty<LocalDateTime> fetchTime;
    private final ReadOnlyStringProperty metar;

    public Metar(final String metar, final LocalDateTime fetchTime) {
        this.fetchTime = new ImmutableObjectProperty<>(fetchTime);
        this.metar = new ImmutableStringProperty(metar);
    }

    public LocalDateTime getFetchTime() {
        return fetchTime.get();
    }

    public ReadOnlyObjectProperty<LocalDateTime> fetchTimeProperty() {
        return fetchTime;
    }

    public String getMetar() {
        return metar.get();
    }

    public ReadOnlyStringProperty metarProperty() {
        return metar;
    }
}
