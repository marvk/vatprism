package net.marvk.fs.vatsim.map.view.table;

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
    private final Repository<ViewModel> clientRepository;
    private final ReadOnlyStringWrapper query = new ReadOnlyStringWrapper();
    private final ReadOnlyObjectWrapper<Pattern> pattern = new ReadOnlyObjectWrapper<>();
    private final ObjectProperty<DataVisitor<Boolean>> predicate = new SimpleObjectProperty<>();
    private final FilteredList<ViewModel> filteredItems;

    @InjectScope
    private StatusScope statusScope;

    public SimpleTableViewModel(final Repository<ViewModel> clientRepository) {
        this.clientRepository = clientRepository;
        this.filteredItems = new FilteredList<>(clientRepository.list());
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

    protected abstract boolean matchesQuery(final ViewModel e);

    @Override
    public ObservableList<ViewModel> items() {
        return filteredItems;
    }
}
