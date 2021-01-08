package net.marvk.fs.vatsim.map.view.datatable.clientstable;

import com.google.inject.Inject;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;
import net.marvk.fs.vatsim.map.view.preferences.Preferences;

public class ClientsTableViewModel extends SimpleTableViewModel<Client> {
    @Inject
    public ClientsTableViewModel(final Preferences preferences) {
        super(preferences);
    }

    @Override
    public ObservableList<Client> items() {
        return toolbarScope.filteredClients();
    }
}
