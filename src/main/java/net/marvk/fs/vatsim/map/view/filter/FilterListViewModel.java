package net.marvk.fs.vatsim.map.view.filter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.map.data.Filter;
import net.marvk.fs.vatsim.map.data.ImmutableObjectProperty;

import java.util.UUID;

public class FilterListViewModel {
    private final ReadOnlyObjectProperty<UUID> uuid;
    private final ObjectProperty<Filter> filter = new SimpleObjectProperty<>();
    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();

    public FilterListViewModel(final Filter filter) {
        this.uuid = new ImmutableObjectProperty<>(filter.getUuid());
        this.filter.set(filter);
        this.name.bind(Bindings.createStringBinding(
                () -> this.filter.get().getName(),
                this.filter
        ));
    }

    public FilterListViewModel getSelf() {
        return this;
    }

    public UUID getUuid() {
        return uuid.get();
    }

    public ReadOnlyObjectProperty<UUID> uuidProperty() {
        return uuid;
    }

    public Filter getFilter() {
        return filter.get();
    }

    public ObjectProperty<Filter> filterProperty() {
        return filter;
    }

    public void setFilter(final Filter filter) {
        this.filter.set(filter);
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }

}
