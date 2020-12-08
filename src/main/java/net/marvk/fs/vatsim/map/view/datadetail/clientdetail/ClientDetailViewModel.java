package net.marvk.fs.vatsim.map.view.datadetail.clientdetail;

import com.google.inject.Inject;
import javafx.application.HostServices;
import net.marvk.fs.vatsim.api.VatsimApiUrlProvider;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubViewModel;

public class ClientDetailViewModel extends DataDetailSubViewModel<Client> {
    private final HostServices hostServices;
    private final VatsimApiUrlProvider urlProvider;

    @Inject
    public ClientDetailViewModel(final HostServices hostServices, final VatsimApiUrlProvider urlProvider) {
        this.hostServices = hostServices;
        this.urlProvider = urlProvider;
    }

    public void openStats() {
        if (getData() != null) {
            hostServices.showDocument(urlProvider.stats(getData().getCid()));
        }
    }
}
