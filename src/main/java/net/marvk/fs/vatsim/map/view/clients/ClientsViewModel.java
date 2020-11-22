package net.marvk.fs.vatsim.map.view.clients;

import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.ClientViewModel;
import net.marvk.fs.vatsim.map.view.GlobalScope;

public class ClientsViewModel implements ViewModel {
    @InjectScope
    private GlobalScope globalScope;

    private final ObjectProperty<ClientViewModel> selectedClient = new SimpleObjectProperty<>();

    public void initialize() {
    }

    public ObservableList<ClientViewModel> getClients() {
        return globalScope.getClients();
    }

    public ClientViewModel getSelectedClient() {
        return selectedClient.get();
    }

    public ObjectProperty<ClientViewModel> selectedClientProperty() {
        return selectedClient;
    }

    public void setSelectedClient(final ClientViewModel selectedClient) {
        this.selectedClient.set(selectedClient);
    }
}
