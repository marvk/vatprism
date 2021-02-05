package net.marvk.fs.vatsim.map.view.datadetail.controllersdetail;

import com.google.inject.Inject;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.TimeProvider;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubViewModel;

import java.time.ZonedDateTime;

public class ControllersDetailViewModel extends DetailSubViewModel<ObservableList<Controller>> {
    private final Preferences preferences;
    private final TimeProvider timeProvider;

    @Inject
    public ControllersDetailViewModel(final Preferences preferences, final TimeProvider timeProvider) {
        this.preferences = preferences;
        this.timeProvider = timeProvider;
    }

    public ZonedDateTime getCurrentTime() {
        return timeProvider.getCurrentTime();
    }

    public ReadOnlyObjectProperty<ZonedDateTime> currentTimeProperty() {
        return timeProvider.currentTimeProperty();
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
