package net.marvk.fs.vatsim.map.view.datatable.streamerstable;

import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

import java.time.LocalDateTime;

public class StreamersTableViewModel extends SimpleTableViewModel<Client> {
    private final HostServices hostServices;
    private final FilteredList<Client> filteredList;
    private final ObservableList<Client> clients;
    private final ObjectProperty<LocalDateTime> currentTime = new SimpleObjectProperty<>();

    @Inject
    public StreamersTableViewModel(final HostServices hostServices, final Preferences preferences, final ClientRepository clientRepository) {
        super(preferences);
        this.hostServices = hostServices;

        this.filteredList = new FilteredList<>(clientRepository.list());
        this.filteredList.predicateProperty().bind(Bindings.createObjectBinding(
                () -> e -> e.getUrls().isTwitch(),
                currentTime
        ));
        Notifications.CLIENTS_RELOADED.subscribe(() -> currentTime.set(LocalDateTime.now()));
        this.clients = FXCollections.unmodifiableObservableList(filteredList);
    }

    @Override
    public ObservableList<Client> items() {
        return clients;
    }

    public void openStream(final Client client) {
        final String twitchUrl = client.getUrls().getTwitchUrl();
        if (twitchUrl != null) {
            hostServices.showDocument("https://" + twitchUrl);
        }
    }
}
