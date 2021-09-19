package net.marvk.fs.vatsim.map.view.datatable.eventstable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Event;
import net.marvk.fs.vatsim.map.data.EventRepository;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.RepositoryException;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class EventsTableViewModel extends SimpleTableViewModel<Event> {
    private final EventRepository eventRepository;

    @Inject
    public EventsTableViewModel(final Preferences preferences, final EventRepository eventRepository) {
        super(preferences);
        this.eventRepository = eventRepository;
    }

    @Override
    public ObservableList<Event> items() {
        try {
            eventRepository.reload();
            return eventRepository.list();
        } catch (final RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
