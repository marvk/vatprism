package net.marvk.fs.vatsim.map.view.clients;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.view.StatusbarScope;
import net.marvk.fs.vatsim.map.view.table.SimpleTableViewModel;

public class ClientsViewModel extends SimpleTableViewModel<Client> {
    @InjectScope
    private StatusbarScope statusbarScope;

    @Inject
    public ClientsViewModel(final ClientRepository clientRepository) {
        super(clientRepository);
    }
}
