package net.marvk.fs.vatsim.map.data;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import net.marvk.fs.vatsim.api.data.VatsimEvent;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

public class Event implements Settable<VatsimEvent>, Data {
    private final ReadOnlyIntegerWrapper id = new ReadOnlyIntegerWrapper();

    public int getId() {
        return id.get();
    }

    public ReadOnlyIntegerProperty idProperty() {
        return id.getReadOnlyProperty();
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

    private final ReadOnlyObjectWrapper<ZonedDateTime> startTime = new ReadOnlyObjectWrapper<>();

    public ZonedDateTime getStartTime() {
        return startTime.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> startTimeProperty() {
        return startTime.getReadOnlyProperty();
    }

    private final ReadOnlyObjectWrapper<ZonedDateTime> endTime = new ReadOnlyObjectWrapper<>();

    public ZonedDateTime getEndTime() {
        return endTime.get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> endTimeProperty() {
        return endTime.getReadOnlyProperty();
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

    private final ReadOnlyObjectWrapper<Duration> duration = new ReadOnlyObjectWrapper<>();

    public Duration getDuration() {
        return duration.get();
    }

    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return duration.getReadOnlyProperty();
    }

    public Event() {
        duration.bind(Bindings.createObjectBinding(this::calculateDuration, startTime, endTime));
    }

    private Duration calculateDuration() {
        final ZonedDateTime start = startTime.get();
        final ZonedDateTime end = endTime.get();

        if (start == null || end == null) {
            return null;
        }

        return Duration.between(start, end);
    }

    @Override
    public void setFromModel(final VatsimEvent model) {
        id.set(model.getId());
        type.set(model.getType());
        vso.set(model.getVsoName());
        name.set(model.getName());
        shortDescription.set(model.getShortDescription());
        description.set(model.getDescription());
        bannerUrl.set(model.getBanner());
        startTime.set(model.getStartTime());
        endTime.set(model.getEndTime());
        organizers.setAll(model.getOrganizers().stream().map(EventOrganizer::new).collect(Collectors.toList()));
    }

    @Override
    public <R> R visit(final DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
