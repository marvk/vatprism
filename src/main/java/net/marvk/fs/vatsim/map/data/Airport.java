package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Objects;

public class Airport implements Settable<AirportRepository.VatsimAirportWrapper>, Data {
    private final StringProperty icao = new SimpleStringProperty();
    private final ReadOnlyListWrapper<String> names = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(new ArrayList<>(1)));
    private final ReadOnlyListWrapper<String> iatas = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(new ArrayList<>(1)));
    private final BooleanProperty pseudo = new SimpleBooleanProperty();
    private final ObjectProperty<Point2D> position = new SimpleObjectProperty<>();

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
        names.setAll(model.getNames());
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

    public ObservableList<String> getNames() {
        return names.getReadOnlyProperty();
    }

    public ObservableList<String> getIatas() {
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

    ObservableList<FlightPlan> getDepartingWritable() {
        return departing;
    }

    public ObservableList<FlightPlan> getDeparting() {
        return departing.getReadOnlyProperty();
    }

    ObservableList<FlightPlan> getArrivingWritable() {
        return arriving;
    }

    public ObservableList<FlightPlan> getArriving() {
        return arriving.getReadOnlyProperty();
    }

    ObservableList<Controller> getControllersWritable() {
        return controllers;
    }

    public ObservableList<Controller> getControllers() {
        return controllers.getReadOnlyProperty();
    }

    ObservableList<FlightPlan> getAlternativesWritable() {
        return alternatives;
    }

    public ObservableList<FlightPlan> alternativesProperty() {
        return alternatives.getReadOnlyProperty();
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
