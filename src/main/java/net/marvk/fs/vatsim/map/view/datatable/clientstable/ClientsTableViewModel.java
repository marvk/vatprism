package net.marvk.fs.vatsim.map.view.datatable.clientstable;

import com.google.inject.Inject;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

public class ClientsTableViewModel extends SimpleTableViewModel<Client> {
    @Inject
    public ClientsTableViewModel(final ClientRepository repository) {
        super(repository);
    }
}
