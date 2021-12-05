package net.marvk.fs.vatsim.map.view.events;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.data.Event;
import net.marvk.fs.vatsim.map.data.EventRepository;
import net.marvk.fs.vatsim.map.data.TimeProvider;

import java.time.ZonedDateTime;

public class EventsViewModel implements ViewModel {
    private final EventRepository eventRepository;
    private final TimeProvider timeProvider;

    @Inject
    public EventsViewModel(
            final EventRepository eventRepository,
            final TimeProvider timeProvider
    ) {
        this.eventRepository = eventRepository;
        this.timeProvider = timeProvider;
    }

    @SneakyThrows
    public ObservableList<Event> events() {
        eventRepository.reload();
        return eventRepository.list();
    }

    public ZonedDateTime getNow() {
        return timeProvider.currentTimeProperty().get();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> nowProperty() {
        return timeProvider.currentTimeProperty();
    }
}
