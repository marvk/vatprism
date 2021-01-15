package net.marvk.fs.vatsim.map.view.datatable;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.data.DataVisitor;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.SimplePredicatesDataVisitor;
import net.marvk.fs.vatsim.map.view.StatusScope;
import net.marvk.fs.vatsim.map.view.ToolbarScope;

import java.util.regex.Pattern;

public abstract class SimpleTableViewModel<ViewModel extends Data> extends AbstractTableViewModel<ViewModel> {
    private final ReadOnlyStringWrapper query = new ReadOnlyStringWrapper();
    private final ReadOnlyObjectWrapper<Pattern> pattern = new ReadOnlyObjectWrapper<>();
    private final ObjectProperty<DataVisitor<Boolean>> predicate = new SimpleObjectProperty<>();
    private final Preferences preferences;

    @InjectScope
    protected StatusScope statusScope;

    @Inject
    protected ToolbarScope toolbarScope;

    @Inject
    public SimpleTableViewModel(final Preferences preferences) {
        this.preferences = preferences;
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

    public ReadOnlyIntegerProperty fontSizeProperty() {
        return preferences.integerProperty("general.font_size");
    }
}
