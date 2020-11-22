package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.data.ClientViewModel;

import java.util.Collection;

public class ClientRepository extends ProviderRepository<VatsimClient, ClientViewModel> {
    @Inject
    public ClientRepository(final VatsimApi vatsimApi, final Provider<ClientViewModel> clientViewModelProvider) {
        super(vatsimApi, clientViewModelProvider);
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
