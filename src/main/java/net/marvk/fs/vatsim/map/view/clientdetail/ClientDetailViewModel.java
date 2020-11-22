package net.marvk.fs.vatsim.map.view.clientdetail;

import de.saxsys.mvvmfx.ViewModel;
import net.marvk.fs.vatsim.map.data.ClientViewModel;

public class ClientDetailViewModel implements ViewModel {
    private final ClientViewModel client = new ClientViewModel();

    public ClientViewModel getClient() {
        return client;
    }
}
