package net.marvk.fs.vatsim.map.view.filter;

import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.marvk.fs.vatsim.map.data.Filter;

public class FilterScope implements Scope {

    private final ObjectProperty<Filter> filter = new SimpleObjectProperty<>();

    public Filter getFilter() {
        return filter.get();
    }

    public ObjectProperty<Filter> filterProperty() {
        return filter;
    }

    public void setFilter(final Filter filter) {
        this.filter.set(filter);
    }
}
