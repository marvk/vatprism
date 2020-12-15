package net.marvk.fs.vatsim.map.view.datatable;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.DataVisitor;
import net.marvk.fs.vatsim.map.data.Repository;
import net.marvk.fs.vatsim.map.data.SimplePredicatesDataVisitor;
import net.marvk.fs.vatsim.map.view.StatusScope;

import java.util.regex.Pattern;

public abstract class SimpleTableViewModel<ViewModel extends Data> extends AbstractTableViewModel<ViewModel> {
    private final ReadOnlyStringWrapper query = new ReadOnlyStringWrapper();
    private final ReadOnlyObjectWrapper<Pattern> pattern = new ReadOnlyObjectWrapper<>();
    private final ObjectProperty<DataVisitor<Boolean>> predicate = new SimpleObjectProperty<>();
    private final FilteredList<ViewModel> filteredItems;

    @InjectScope
    private StatusScope statusScope;

    @Inject
    public SimpleTableViewModel(final Repository<ViewModel> dataRepository) {
        this.filteredItems = new FilteredList<>(dataRepository.list());
    }

    public void initialize() {
        query.bind(statusScope.searchQueryProperty());
        pattern.bind(Bindings.createObjectBinding(
                () -> Pattern.compile(Pattern.quote(query.get()), Pattern.CASE_INSENSITIVE),
                query
        ));
        predicate.bind(Bindings.createObjectBinding(
                () -> SimplePredicatesDataVisitor.nullOrBlankIsTrue(query.get()),
                query
        ));
        filteredItems.predicateProperty().bind(Bindings.createObjectBinding(
                () -> e -> predicate.get().visit(e),
                predicate
        ));
    }

    public String getQuery() {
        return query.get();
    }

    public ReadOnlyStringProperty queryProperty() {
        return query.getReadOnlyProperty();
    }

    public Pattern getPattern() {
        return pattern.get();
    }

    public ReadOnlyObjectProperty<Pattern> patternProperty() {
        return pattern.getReadOnlyProperty();
    }

    @Override
    public ObservableList<ViewModel> items() {
        return filteredItems;
    }
}
