package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class Airport implements Settable<AirportRepository.VatsimAirportWrapper>, Data {
    private final StringProperty icao = new SimpleStringProperty();
    private final ReadOnlyListWrapper<ReadOnlyStringProperty> names = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(new ArrayList<>(1)));
    private final ReadOnlyListWrapper<String> iatas = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(new ArrayList<>(1)));
    private final BooleanProperty pseudo = new SimpleBooleanProperty();
    private final ObjectProperty<Point2D> position = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<FlightInformationRegionBoundary> flightInformationRegionBoundary =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, FlightInformationRegionBoundary::getAirportsWritable);

    private final ReadOnlyListWrapper<Controller> controllers =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, Controller::workingAirportPropertyWritable);

    private final ReadOnlyListWrapper<FlightPlan> departing =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightPlan::departureAirportPropertyWritable);

    private final ReadOnlyListWrapper<FlightPlan> arriving =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightPlan::arrivalAirportPropertyWritable);

    private final ReadOnlyListWrapper<FlightPlan> alternatives =
            RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightPlan::alternativeAirportPropertyWritable);

    @Override
    public void setFromModel(final AirportRepository.VatsimAirportWrapper model) {
        Objects.requireNonNull(model);

        icao.set(model.getIcao());
        names.setAll(model
                .getNames()
                .stream()
                .distinct()
                .map(ImmutableStringProperty::new)
                .collect(Collectors.toList())
        );
        iatas.setAll(model.getIatas());
        pseudo.set(model.isPseudo());

        position.set(model.getPosition());
    }

    public String getIcao() {
        return icao.get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

    public ReadOnlyListProperty<ReadOnlyStringProperty> getNames() {
        return names.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<String> getIatas() {
        return iatas.getReadOnlyProperty();
    }

    public boolean isPseudo() {
        return pseudo.get();
    }

    public ReadOnlyBooleanProperty pseudoProperty() {
        return pseudo;
    }

    public Point2D getPosition() {
        return position.get();
    }

    public ReadOnlyObjectProperty<Point2D> positionProperty() {
        return position;
    }

    SimpleListProperty<FlightPlan> getDepartingWritable() {
        return departing;
    }

    public ReadOnlyListProperty<FlightPlan> getDeparting() {
        return departing.getReadOnlyProperty();
    }

    SimpleListProperty<FlightPlan> getArrivingWritable() {
        return arriving;
    }

    public ReadOnlyListProperty<FlightPlan> getArriving() {
        return arriving.getReadOnlyProperty();
    }

    SimpleListProperty<Controller> getControllersWritable() {
        return controllers;
    }

    public ReadOnlyListProperty<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    SimpleListProperty<FlightPlan> getAlternativesWritable() {
        return alternatives;
    }

    public ReadOnlyListProperty<FlightPlan> alternativesProperty() {
        return alternatives.getReadOnlyProperty();
    }

    public FlightInformationRegionBoundary getFlightInformationRegionBoundary() {
        return flightInformationRegionBoundary.get();
    }

    ObjectProperty<FlightInformationRegionBoundary> flightInformationRegionBoundaryPropertyWritable() {
        return flightInformationRegionBoundary;
    }

    public ReadOnlyObjectProperty<FlightInformationRegionBoundary> flightInformationRegionBoundaryProperty() {
        return flightInformationRegionBoundary.getReadOnlyProperty();
    }

    public boolean hasArrivals() {
        return !arriving.isEmpty();
    }

    public boolean hasDepartures() {
        return !departing.isEmpty();
    }

    public boolean hasAlternatives() {
        return !alternatives.isEmpty();
    }

    public boolean hasControllers() {
        return !controllers.isEmpty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
