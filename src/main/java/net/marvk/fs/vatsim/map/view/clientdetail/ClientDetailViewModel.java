package net.marvk.fs.vatsim.map.view.clientdetail;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import net.marvk.fs.vatsim.map.data.Client;

public class ClientDetailViewModel implements ViewModel {
    private final Client client;

    @Inject
    public ClientDetailViewModel(final Client clientViewModel) {
        this.client = clientViewModel;
    }

    public Client getClient() {
        return client;
    }
}
