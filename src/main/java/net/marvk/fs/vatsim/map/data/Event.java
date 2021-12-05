package net.marvk.fs.vatsim.map.data;

import com.calendarfx.model.Entry;
import com.calendarfx.model.Interval;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import net.marvk.fs.vatsim.api.data.VatsimEvent;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

public class Event extends Entry<Event> implements Settable<VatsimEvent>, Data {
    public Event() {
        duration0.bind(Bindings.createObjectBinding(this::calculateDuration, startTime0, endTime0));

        intervalProperty().bind(Bindings.createObjectBinding(() -> {
            if (getStartTime0() != null && getEndTime0() != null) {
                return new Interval(getStartTime0(), getEndTime0());
            }

            return new Interval();
        }, startTime0Property(), endTime0Property()));
        titleProperty().bind(nameProperty());
        locationProperty().bind(Bindings.createStringBinding(
                () -> airports.stream().map(Airport::getIcao).collect(Collectors.joining(", ")),
                getAirports()
        ));
    }

    private final ReadOnlyIntegerWrapper vatsimId = new ReadOnlyIntegerWrapper();

    public int getVatsimId() {
        return vatsimId.get();
    }

    public ReadOnlyIntegerProperty vatsimIdProperty() {
        return vatsimId.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper type = new ReadOnlyStringWrapper();

    public String getType() {
        return type.get();
    }

    public ReadOnlyStringProperty typeProperty() {
        return type.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper vso = new ReadOnlyStringWrapper();

    public String getVso() {
        return vso.get();
    }

    public ReadOnlyStringProperty vsoProperty() {
        return vso.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper shortDescription = new ReadOnlyStringWrapper();

    public String getShortDescription() {
        return shortDescription.get();
    }

    public ReadOnlyStringProperty shortDescriptionProperty() {
        return shortDescription.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper();

    public String getDescription() {
        return description.get();
    }

    public ReadOnlyStringProperty descriptionProperty() {
        return description.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper bannerUrl = new ReadOnlyStringWrapper();

    public String getBannerUrl() {
        return bannerUrl.get();
    }

    public ReadOnlyStringProperty bannerUrlProperty() {
        return bannerUrl.getReadOnlyProperty();
    }

    private final ReadOnlyObjectWrapper<ZonedDateTime> startTime0 = new ReadOnlyObjectWrapper<>();

    public ZonedDateTime getStartTime0() {
        return startTime0.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> startTime0Property() {
        return startTime0.getReadOnlyProperty();
    }

    private final ReadOnlyObjectWrapper<ZonedDateTime> endTime0 = new ReadOnlyObjectWrapper<>();

    public ZonedDateTime getEndTime0() {
        return endTime0.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> endTime0Property() {
        return endTime0.getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<Airport> airports;

    ReadOnlyListWrapper<Airport> getAirportsWritable() {
        if (airports == null) {
            airports = RelationshipReadOnlyListWrapper.withOtherList(this, Airport::getEventsWritable);
        }
        return airports;
    }

    public ReadOnlyListProperty<Airport> getAirports() {
        return getAirportsWritable().getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<EventOrganizer> organizers = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    ReadOnlyListWrapper<EventOrganizer> getOrganizersWritable() {
        return organizers;
    }

    public ReadOnlyListProperty<EventOrganizer> getOrganizers() {
        return organizers.getReadOnlyProperty();
    }

    private ReadOnlyListWrapper<EventRoute> routes = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    ReadOnlyListWrapper<EventRoute> getRoutesWritable() {
        return routes;
    }

    public ReadOnlyListProperty<EventRoute> getRoutes() {
        return routes.getReadOnlyProperty();
    }

    private final ReadOnlyObjectWrapper<Duration> duration0 = new ReadOnlyObjectWrapper<>();

    private Duration getDuration0() {
        return duration0.get();
    }

    private ReadOnlyObjectProperty<Duration> duration0Property() {
        return duration0.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return duration0.getReadOnlyProperty();
    }

    private Duration calculateDuration() {
        final ZonedDateTime start = startTime0.get();
        final ZonedDateTime end = endTime0.get();

        if (start == null || end == null) {
            return null;
        }

        return Duration.between(start, end);
    }

    @Override
    public void setFromModel(final VatsimEvent model) {
        vatsimId.set(model.getId());
        type.set(model.getType());
        vso.set(model.getVsoName());
        name.set(model.getName());
        shortDescription.set(model.getShortDescription());
        description.set(model.getDescription());
        bannerUrl.set(model.getBanner());
        startTime0.set(model.getStartTime());
        endTime0.set(model.getEndTime());
        organizers.setAll(model.getOrganizers().stream().map(EventOrganizer::new).collect(Collectors.toList()));
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
