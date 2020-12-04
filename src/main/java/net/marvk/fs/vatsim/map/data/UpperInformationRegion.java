package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.api.data.VatsimUpperInformationRegion;

import java.util.Objects;

public class UpperInformationRegion implements Settable<VatsimUpperInformationRegion>, Data {
    private final StringProperty icao =
            new SimpleStringProperty();

    private final StringProperty name =
            new SimpleStringProperty();

    private final ReadOnlyListWrapper<Controller> controllers =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Controller::workingUpperInformationRegionPropertyWritable);

    private final ReadOnlyListWrapper<FlightInformationRegionBoundary> flightInformationRegionBoundaries =
            RelationshipReadOnlyListWrapper.withOtherList(this, FlightInformationRegionBoundary::getUpperInformationRegionsWritable);

    @Override
    public void setFromModel(final VatsimUpperInformationRegion model) {
        Objects.requireNonNull(model);

        icao.set(model.getIcao());
        name.set(model.getName());
    }

    public String getIcao() {
        return icao.get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    ObservableList<Controller> getControllersWritable() {
        return controllers;
    }

    public ObservableList<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    ObservableList<FlightInformationRegionBoundary> getFlightInformationRegionBoundariesWritable() {
        return flightInformationRegionBoundaries;
    }

    public ObservableList<FlightInformationRegionBoundary> getFlightInformationRegionBoundaries() {
        return flightInformationRegionBoundaries.getReadOnlyProperty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
