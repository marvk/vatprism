package net.marvk.fs.vatsim.map.data;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class Airport implements Settable<AirportRepository.VatsimAirportWrapper>, Data {
    private ReadOnlyStringProperty icao;

    public String getIcao() {
        return icaoProperty().get();
    }

    public ReadOnlyStringProperty icaoProperty() {
        return icao;
    }

//    private ReadOnlyBooleanProperty pseudo;
//
//    public boolean isPseudo() {
//        return pseudo.get();
//    }
//
//    public ReadOnlyBooleanProperty pseudoProperty() {
//        return pseudo;
//    }

    private ReadOnlyObjectProperty<Point2D> position;

    public Point2D getPosition() {
        return position.get();
    }

    public ReadOnlyObjectProperty<Point2D> positionProperty() {
        return position;
    }

    private IntegerProperty trafficCount;

    public int getTrafficCount() {
        return trafficCount.get();
    }

    public ReadOnlyIntegerProperty trafficCountProperty() {
        if (trafficCount == null) {
            trafficCount = new SimpleIntegerProperty();
            bindTrafficCount();
        }
        return trafficCount;
    }

    private ReadOnlyListProperty<ReadOnlyStringProperty> names;

    public ReadOnlyListProperty<ReadOnlyStringProperty> getNames() {
        return names;
    }

    private ReadOnlyListProperty<String> iatas;

    public ReadOnlyListProperty<String> getIatas() {
        return iatas;
    }

    private final ReadOnlyObjectWrapper<FlightInformationRegionBoundary> flightInformationRegionBoundary =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, FlightInformationRegionBoundary::airportsWritable);

    public FlightInformationRegionBoundary getFlightInformationRegionBoundary() {
        return flightInformationRegionBoundaryPropertyWritable().get();
    }

    ReadOnlyObjectWrapper<FlightInformationRegionBoundary> flightInformationRegionBoundaryPropertyWritable() {
        return flightInformationRegionBoundary;
    }

    public ReadOnlyObjectProperty<FlightInformationRegionBoundary> flightInformationRegionBoundaryProperty() {
        return flightInformationRegionBoundaryPropertyWritable().getReadOnlyProperty();
    }

    private final ReadOnlyObjectWrapper<Country> country =
            RelationshipReadOnlyObjectWrapper.withOtherList(this, Country::airportsWritable);

    public Country getCountry() {
        return countryPropertyWritable().get();
    }

    ReadOnlyObjectWrapper<Country> countryPropertyWritable() {
        return country;
    }

    public ReadOnlyObjectProperty<Country> countryProperty() {
        return countryPropertyWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<Controller> controllers;

    ReadOnlyListWrapper<Controller> getControllersWritable() {
        if (controllers == null) {
            controllers = RelationshipReadOnlyListWrapper.withOtherProperty(this, Controller::workingAirportPropertyWritable);
        }
        return controllers;
    }

    public ReadOnlyListProperty<Controller> getControllers() {
        return getControllersWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<FlightPlan> departing;

    ReadOnlyListWrapper<FlightPlan> getDepartingWritable() {
        if (departing == null) {
            departing = RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightPlan::departureAirportPropertyWritable);
            bindTrafficCount();
        }
        return departing;
    }

    public ReadOnlyListProperty<FlightPlan> getDeparting() {
        return getDepartingWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<FlightPlan> arriving;

    ReadOnlyListWrapper<FlightPlan> getArrivingWritable() {
        if (arriving == null) {
            arriving = RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightPlan::arrivalAirportPropertyWritable);
            bindTrafficCount();
        }
        return arriving;
    }

    public ReadOnlyListProperty<FlightPlan> getArriving() {
        return getArrivingWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<FlightPlan> alternatives;

    ReadOnlyListWrapper<FlightPlan> getAlternativesWritable() {
        if (alternatives == null) {
            alternatives = RelationshipReadOnlyListWrapper.withOtherProperty(this, FlightPlan::alternativeAirportPropertyWritable);
        }
        return alternatives;
    }

    public ReadOnlyListProperty<FlightPlan> alternativesProperty() {
        return getAlternativesWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<EventRoute> arrivalInEventRoutes;

    ReadOnlyListWrapper<EventRoute> getArrivalInEventRoutesWritable() {
        if (arrivalInEventRoutes == null) {
            arrivalInEventRoutes = RelationshipReadOnlyListWrapper.withOtherProperty(this, EventRoute::getArrivalWritable);
        }
        return arrivalInEventRoutes;
    }

    public ReadOnlyListProperty<FlightPlan> arrivalInEventRoutesProperty() {
        return getAlternativesWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<EventRoute> departureInEventRoutes;

    ReadOnlyListWrapper<EventRoute> getDepartureInEventRoutesWritable() {
        if (departureInEventRoutes == null) {
            departureInEventRoutes = RelationshipReadOnlyListWrapper.withOtherProperty(this, EventRoute::getDepartureWritable);
        }
        return departureInEventRoutes;
    }

    public ReadOnlyListProperty<FlightPlan> departureInEventRoutesProperty() {
        return getAlternativesWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<Event> events;

    ReadOnlyListWrapper<Event> getEventsWritable() {
        if (events == null) {
            events = RelationshipReadOnlyListWrapper.withOtherList(this, Event::getAirportsWritable);
        }
        return events;
    }

    public ReadOnlyListProperty<Event> getEvents() {
        return getEventsWritable().getReadOnlyProperty();
    }

    @Override
    public void setFromModel(final AirportRepository.VatsimAirportWrapper model) {
        Objects.requireNonNull(model);

        icao = new ImmutableStringProperty(model.getIcao());
        final List<ReadOnlyStringProperty> modelNames = model
                .getNames()
                .stream()
                .map(ImmutableStringProperty::new)
                .collect(Collectors.toUnmodifiableList());
        names = new ImmutableListProperty<>(modelNames);
        iatas = new ImmutableListProperty<>(model.getIatas());
        position = new ImmutableObjectProperty<>(model.getPosition());
//        pseudo = new ImmutableBooleanProperty(model.isPseudo());
    }

    private void bindTrafficCount() {
        if (trafficCount != null) {
            if (departing != null && arriving != null) {
                trafficCount.bind(Bindings.add(departing.sizeProperty(), arriving.sizeProperty()));
            } else if (departing != null) {
                trafficCount.bind(departing.sizeProperty());
            } else if (arriving != null) {
                trafficCount.bind(arriving.sizeProperty());
            } else {
                trafficCount.set(0);
            }
        }
    }

    public boolean hasArrivals() {
        return arriving != null && !arriving.isEmpty();
    }

    public boolean hasDepartures() {
        return departing != null && !departing.isEmpty();
    }

    public boolean hasAlternatives() {
        return alternatives != null && !alternatives.isEmpty();
    }

    public boolean hasControllers() {
        return controllers != null && !controllers.isEmpty();
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
