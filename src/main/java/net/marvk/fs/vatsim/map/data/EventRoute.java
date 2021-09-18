package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import net.marvk.fs.vatsim.api.data.VatsimEvent;

public class EventRoute implements Settable<VatsimEvent.Route> {
    private ReadOnlyObjectWrapper<Airport> departure;

    ReadOnlyObjectWrapper<Airport> getDepartureWritable() {
        if (departure == null) {
            departure = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getDepartureInEventRoutesWritable);
        }
        return departure;
    }

    public Airport getDeparture() {
        return getDepartureWritable().get();
    }

    public ReadOnlyObjectProperty<Airport> departureProperty() {
        return getDepartureWritable().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Airport> arrival;

    ReadOnlyObjectWrapper<Airport> getArrivalWritable() {
        if (arrival == null) {
            arrival = RelationshipReadOnlyObjectWrapper.withOtherList(this, Airport::getArrivalInEventRoutesWritable);
        }
        return arrival;
    }

    public Airport getArrival() {
        return getArrivalWritable().get();
    }

    public ReadOnlyObjectProperty<Airport> arrivalProperty() {
        return getArrivalWritable().getReadOnlyProperty();
    }

    private final ReadOnlyObjectWrapper<Route> route = new ReadOnlyObjectWrapper<>();

    public Route getRoute() {
        return route.get();
    }

    public ReadOnlyObjectProperty<Route> routeProperty() {
        return route.getReadOnlyProperty();
    }

    @Override
    public void setFromModel(final VatsimEvent.Route route) {
        this.route.set(Route.parse(route.getRoute()));
    }
}
