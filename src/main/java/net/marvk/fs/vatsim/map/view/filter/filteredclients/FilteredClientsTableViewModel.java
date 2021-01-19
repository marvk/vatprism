package net.marvk.fs.vatsim.map.view.filter.filteredclients;

import com.google.inject.Inject;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.Client;
import net.marvk.fs.vatsim.map.data.ClientRepository;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.view.datatable.SimpleTableViewModel;

import java.util.function.Predicate;

public class FilteredClientsTableViewModel extends SimpleTableViewModel<Client> {
    private final FilteredList<Client> clients;
    private final SimpleObjectProperty<Predicate<Client>> predicate = new SimpleObjectProperty<>(e -> true);

    @Inject
    public FilteredClientsTableViewModel(final ClientRepository clientRepository, final Preferences preferences) {
        super(preferences);

        this.clients = new FilteredList<>(clientRepository.list());
        this.clients.predicateProperty().bind(predicate);
    }

    @Override
    public ObservableList<Client> items() {
        return FXCollections.unmodifiableObservableList(clients);
    }

    public Predicate<Client> getPredicate() {
        return predicate.get();
    }

    public SimpleObjectProperty<Predicate<Client>> predicateProperty() {
        return predicate;
    }

    public void setPredicate(final Predicate<Client> predicate) {
        this.predicate.set(predicate);
    }
}
