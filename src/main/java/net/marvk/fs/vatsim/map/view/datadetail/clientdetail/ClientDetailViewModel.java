package net.marvk.fs.vatsim.map.view.datadetail.clientdetail;

import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.api.VatsimApiUrlProvider;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

public class ClientDetailViewModel extends DataDetailSubViewModel<Client> {
    private final HostServices hostServices;
    private final VatsimApiUrlProvider urlProvider;
    private final ReadOnlyBooleanWrapper livestream = new ReadOnlyBooleanWrapper();
    private final ReadOnlyObjectWrapper<String> livestreamUrl = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper livestreamPlatform = new ReadOnlyStringWrapper();

    @Inject
    public ClientDetailViewModel(final HostServices hostServices, final VatsimApiUrlProvider urlProvider, final Preferences preferences) {
        this.hostServices = hostServices;
        this.urlProvider = urlProvider;

        final BooleanProperty social = preferences.booleanProperty("general.social");

        data.addListener((observable, oldValue, newValue) -> {
            livestream.bind(newValue.getUrls().livestreamProperty().and(social));
            livestreamUrl.bind(newValue.getUrls().urlProperty());
            livestreamPlatform.bind(newValue.getUrls().platformProperty());
        });
    }

    public boolean isLivestream() {
        return livestream.get();
    }

    public ReadOnlyBooleanProperty livestreamProperty() {
        return livestream.getReadOnlyProperty();
    }

    public String getLivestreamPlatform() {
        return livestreamPlatform.get();
    }

    public ReadOnlyStringProperty livestreamPlatformProperty() {
        return livestreamPlatform.getReadOnlyProperty();
    }

    public void openStats() {
        if (getData() != null) {
            hostServices.showDocument(urlProvider.stats(getData().getCidString()));
        }
    }

    public void openStream() {
        if (livestreamUrl.get() != null) {
            hostServices.showDocument("https://" + livestreamUrl.get());
        }
    }
}
