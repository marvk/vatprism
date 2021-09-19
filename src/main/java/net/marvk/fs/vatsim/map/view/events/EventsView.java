package net.marvk.fs.vatsim.map.view.events;

import com.calendarfx.model.Calendar;
import com.calendarfx.view.CalendarView;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

public class EventsView implements FxmlView<EventsViewModel> {
    @FXML
    private HBox container;

    @InjectViewModel
    private EventsViewModel viewModel;

    public void initialize() {
        final CalendarView calendarView = new CalendarView();
        final Calendar calendar = new Calendar("");

        calendar.addEntries();

        container.getChildren().add(calendarView);
    }
}
