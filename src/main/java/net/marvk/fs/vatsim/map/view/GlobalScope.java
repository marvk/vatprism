package net.marvk.fs.vatsim.map.view;

import de.saxsys.mvvmfx.Scope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.ClientViewModel;

public class GlobalScope implements Scope {
    private final ObservableList<ClientViewModel> clients = FXCollections.observableArrayList();

    public ObservableList<ClientViewModel> getClients() {
        return clients;
    }
}
