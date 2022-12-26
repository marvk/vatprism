package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;

import java.util.Comparator;

public class Airline implements Data {
    private final ReadOnlyIntegerProperty airlineId;
    private final ReadOnlyStringProperty name;
    private final ReadOnlyStringProperty alias;
    private final ReadOnlyStringProperty iata;
    private final ReadOnlyStringProperty icao;
    private final ReadOnlyStringProperty callsign;
    private final ReadOnlyStringProperty country;
    private final ReadOnlyBooleanProperty active;

    public Airline(
            final int airlineId,
            final String name,
            final String alias,
            final String iata,
            final String icao,
            final String callsign,
            final String country,
            final boolean active
    ) {
        this.airlineId = new ImmutableIntegerProperty(airlineId);
        this.name = new ImmutableStringProperty(name);
        this.alias = new ImmutableStringProperty(alias);
        this.iata = new ImmutableStringProperty(iata);
        this.icao = new ImmutableStringProperty(icao);
        this.callsign = new ImmutableStringProperty(callsign);
        this.country = new ImmutableStringProperty(country);
        this.active = new ImmutableBooleanProperty(active);
    }

    public int getAirlineId() {
        return airlineId.get();
    }

    public ReadOnlyIntegerProperty airlineIdProperty() {
        return airlineId;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public String getAlias() {
        return alias.get();
    }

    public ReadOnlyStringProperty aliasProperty() {
        return alias;
    }

    public String getIata() {
        return iata.get();
    }

    public ReadOnlyStringProperty iataProperty() {
        return iata;
    }

    public String getIcao() {
        return icao.get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

    public String getCallsign() {
        return callsign.get();
    }

    public ReadOnlyStringProperty callsignProperty() {
        return callsign;
    }

    public String getCountry() {
        return country.get();
    }

    public ReadOnlyStringProperty countryProperty() {
        return country;
    }

    public boolean isActive() {
        return active.get();
    }

    public ReadOnlyBooleanProperty activeProperty() {
        return active;
    }

    public static Comparator<Airline> comparingByIcao() {
        return Comparator.comparing(Airline::getIcao);
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        throw new UnsupportedOperationException();
    }
}
