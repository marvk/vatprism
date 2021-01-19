package net.marvk.fs.vatsim.map.view.filter.filteredclients;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.data.*;

import java.util.function.Predicate;

public class FilteredClientsViewModel implements ViewModel {
    private final ReadOnlyObjectWrapper<Predicate<Client>> predicate = new ReadOnlyObjectWrapper<>();

    private final BooleanProperty controllers = new SimpleBooleanProperty();
    private final BooleanProperty pilots = new SimpleBooleanProperty();

    private final ObservableList<Filter> availableFilters;
    private final ObservableList<Filter> selectedFilters;

    @Inject
    public FilteredClientsViewModel(final FilterRepository filterRepository) {
        availableFilters = filterRepository.list();
        // TODO stub for later
        selectedFilters = availableFilters;

        predicate.bind(Bindings.createObjectBinding(
                this::predicate,
                filterRepository.list(),
                controllers,
                pilots,
                selectedFilters
        ));
    }

    private Predicate<Client> predicate() {
        return this::testClient;
    }

    private boolean testClient(final Client e) {
        return (controllers.get() && e instanceof Controller || pilots.get() && e instanceof Pilot) && isaBoolean(e);
    }

    private boolean isaBoolean(final Client e) {
        return selectedFilters.stream().anyMatch(filter -> filter.isEnabled() && filter.test(e));
    }

    public Predicate<Client> getPredicate() {
        return predicate.get();
    }

    public ReadOnlyObjectProperty<Predicate<Client>> predicateProperty() {
        return predicate.getReadOnlyProperty();
    }

    public boolean isControllers() {
        return controllers.get();
    }

    public BooleanProperty controllersProperty() {
        return controllers;
    }

    public void setControllers(final boolean controllers) {
        this.controllers.set(controllers);
    }

    public boolean isPilots() {
        return pilots.get();
    }

    public BooleanProperty pilotsProperty() {
        return pilots;
    }

    public void setPilots(final boolean pilots) {
        this.pilots.set(pilots);
    }
}

