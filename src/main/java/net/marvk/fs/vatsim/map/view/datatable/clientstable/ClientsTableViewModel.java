package net.marvk.fs.vatsim.map.view.datatable.clientstable;

import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class ClientsTableViewModel extends SimpleTableViewModel<Client> {
    @Override
    public ObservableList<Client> items() {
        return toolbarScope.filteredClients();
    }
}
