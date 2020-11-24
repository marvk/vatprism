package net.marvk.fs.vatsim.map.repository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.marvk.fs.vatsim.api.VatsimApi;
import net.marvk.fs.vatsim.api.VatsimApiException;
import net.marvk.fs.vatsim.api.data.VatsimClient;
import net.marvk.fs.vatsim.map.data.ClientViewModel;

import java.util.*;
import java.util.function.BiFunction;

public class ClientRepository extends ProviderRepository<VatsimClient, ClientViewModel> {
//    private final Map<String, List<ClientViewModel>> callsign = new HashMap<>();

    @Inject
    public ClientRepository(final VatsimApi vatsimApi, final Provider<ClientViewModel> clientViewModelProvider) {
        super(vatsimApi, clientViewModelProvider);
    }

    @Override
    protected String extractKey(final VatsimClient vatsimClient) {
        return vatsimClient.getCallsign() + "_" + vatsimClient.getCid();
    }

    @Override
    protected Collection<VatsimClient> extractModelList(final VatsimApi api) throws VatsimApiException {
        return api.data().getClients();
    }

    @Override
    protected void onAdd(final VatsimClient model, final ClientViewModel toAdd) {
//        final String key = model.getCallsign();
//        callsign.putIfAbsent(key, new ArrayList<>(1));
//        callsign.compute(key, (s, clientViewModels) -> {
//            clientViewModels.add(toAdd);
//            return clientViewModels;
//        });
    }

    @Override
    protected void onRemove(final ClientViewModel toRemove) {
//        final String key = toRemove.callsignProperty().get();
//        final List<ClientViewModel> clientViewModels = callsign.get(key);
//        clientViewModels.remove(toRemove);
//        if (clientViewModels.isEmpty()) {
//            callsign.remove(key);
//        }
    }

    @Override
    protected void onUpdate(final VatsimClient model, final ClientViewModel toUpdate) {
    }
}
