package net.marvk.fs.vatsim.map.repository;

import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.data.ClientViewModel;

import java.util.Collection;

public class ClientRepository extends SimpleRepository<VatsimClient, ClientViewModel> {
    public ClientRepository(final VatsimApi vatsimApi) {
        super(vatsimApi);
    }

    @Override
    protected ClientViewModel map(final VatsimClient vatsimClient) {
        return new ClientViewModel(vatsimClient);
    }

    @Override
    protected String extractKey(final VatsimClient vatsimClient) {
        return vatsimClient.getCid();
    }

    @Override
    protected Collection<VatsimClient> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.data().getClients();
    }
}
