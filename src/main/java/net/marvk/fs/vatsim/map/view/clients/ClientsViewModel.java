package net.marvk.fs.vatsim.map.view.clients;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.view.table.SimpleTableViewModel;

public class ClientsViewModel extends SimpleTableViewModel<Client> {
    @Inject
    public ClientsViewModel(final ClientRepository clientRepository) {
        super(clientRepository);
    }

    @Override
    protected boolean matchesQuery(final Client e) {
        return false;
    }
}
