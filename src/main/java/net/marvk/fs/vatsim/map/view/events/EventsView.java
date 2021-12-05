package net.marvk.fs.vatsim.map.view.events;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;

import java.util.List;
import java.util.stream.Collectors;

public class EventsView implements FxmlView<EventsViewModel> {
    @FXML
    private CalendarView container;

    @InjectViewModel
    private EventsViewModel viewModel;

    public void initialize() {
        final Calendar calendar = new Calendar("");

        final List<Entry<?>> collect = viewModel
                .events()
                .stream()
                .map(e -> (Entry<?>) e)
                .collect(Collectors.toList());

        calendar.addEntries(collect);


        final CalendarSource calendarSource = new CalendarSource();
        calendarSource.getCalendars().add(calendar);
        container.getCalendarSources().add(calendarSource);

        container.setEntryEditPolicy(param -> false);

        System.out.println(calendarSource);
    }
}
