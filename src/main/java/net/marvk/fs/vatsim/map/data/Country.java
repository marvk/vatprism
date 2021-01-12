package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

public class Country implements Settable<CountryRepository.VatsimCountryWrapper> {
    private ReadOnlyStringProperty name;
    private ReadOnlyListWrapper<String> prefixes;
    private ReadOnlyStringProperty discriminator;

    private final ReadOnlyListWrapper<Airport> airports =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Airport::countryPropertyWritable);

    private final ReadOnlyListWrapper<FlightInformationRegionBoundary> firbs =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightInformationRegionBoundary::countryPropertyWritable);

    @Override
    public void setFromModel(final CountryRepository.VatsimCountryWrapper vatsimCountry) {
        this.name = new ImmutableStringProperty(vatsimCountry.getName());
        this.prefixes = new ReadOnlyListWrapper<>(FXCollections.observableList(vatsimCountry.getPrefixes()));
        this.discriminator = new ImmutableStringProperty(vatsimCountry.getDiscriminator());
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public ObservableList<String> getPrefixes() {
        return prefixes.get();
    }

    public ReadOnlyListProperty<String> prefixesProperty() {
        return prefixes.getReadOnlyProperty();
    }

    public String getDiscriminator() {
        return discriminator.get();
    }

    public ReadOnlyStringProperty discriminatorProperty() {
        return discriminator;
    }

    SimpleListProperty<Airport> airportsWritable() {
        return airports;
    }

    public ReadOnlyListProperty<Airport> airports() {
        return airports.getReadOnlyProperty();
    }

    SimpleListProperty<FlightInformationRegionBoundary> flightInformationRegionBoundariesWritable() {
        return firbs;
    }

    public ReadOnlyListProperty<FlightInformationRegionBoundary> flightInformationRegionBoundaries() {
        return firbs.getReadOnlyProperty();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Country country = (Country) o;

        return Objects.equals(name, country.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
