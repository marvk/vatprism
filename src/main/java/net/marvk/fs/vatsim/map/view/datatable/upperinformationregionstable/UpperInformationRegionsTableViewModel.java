package net.marvk.fs.vatsim.map.view.datatable.upperinformationregionstable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

public class UpperInformationRegionsTableViewModel extends SimpleTableViewModel<UpperInformationRegion> {
    @Inject
    public UpperInformationRegionsTableViewModel(final Preferences preferences) {
        super(preferences);
    }

    @Override
    public ObservableList<UpperInformationRegion> items() {
        return toolbarScope.filteredUirs();
    }
}
