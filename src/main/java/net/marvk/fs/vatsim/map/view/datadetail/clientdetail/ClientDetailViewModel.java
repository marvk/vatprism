package net.marvk.fs.vatsim.map.view.datadetail.clientdetail;

import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import net.marvk.fs.vatsim.api.VatsimApiUrlProvider;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

public class ClientDetailViewModel extends DataDetailSubViewModel<Client> {
    private final HostServices hostServices;
    private final VatsimApiUrlProvider urlProvider;
    private final ReadOnlyBooleanWrapper twitchStream = new ReadOnlyBooleanWrapper();
    private final ReadOnlyObjectWrapper<String> twitchStreamUrl = new ReadOnlyObjectWrapper<>();

    @Inject
    public ClientDetailViewModel(final HostServices hostServices, final VatsimApiUrlProvider urlProvider) {
        this.hostServices = hostServices;
        this.urlProvider = urlProvider;

        data.addListener((observable, oldValue, newValue) -> {
            twitchStream.bind(Bindings.createBooleanBinding(
                    () -> newValue.getUrls()
                                  .stream()
                                  .anyMatch(e -> e.contains("twitch")),
                    newValue.getUrls()
            ));
            twitchStreamUrl.bind(Bindings.createObjectBinding(
                    () -> newValue.getUrls()
                                  .stream()
                                  .filter(e -> e.contains("twitch"))
                                  .findFirst()
                                  .orElse(null),
                    newValue.getUrls()
            ));
        });
    }

    public boolean isTwitchStream() {
        return twitchStream.get();
    }

    public ReadOnlyBooleanProperty twitchStreamProperty() {
        return twitchStream.getReadOnlyProperty();
    }

    public void openStats() {
        if (getData() != null) {
            hostServices.showDocument(urlProvider.stats(getData().getCidString()));
        }
    }

    public void openStream() {
        if (twitchStreamUrl.get() != null) {
            hostServices.showDocument(twitchStreamUrl.get());
        }
    }
}
