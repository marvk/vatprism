package net.marvk.fs.vatsim.map.view.clientdetail;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import net.marvk.fs.vatsim.map.data.ClientViewModel;

public class ClientDetailViewModel implements ViewModel {
    private final ClientViewModel client;

    @Inject
    public ClientDetailViewModel(final ClientViewModel clientViewModel) {
        this.client = clientViewModel;
    }

    public ClientViewModel getClient() {
        return client;
    }
}
