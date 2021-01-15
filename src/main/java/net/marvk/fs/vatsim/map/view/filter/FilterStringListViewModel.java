package net.marvk.fs.vatsim.map.view.filter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.map.data.Filter;

public class FilterStringListViewModel {
    private final ReadOnlyObjectWrapper<Filter.StringPredicate> predicate = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper();

    private final BooleanProperty regex = new SimpleBooleanProperty();
    private final StringProperty content = new SimpleStringProperty();

    public FilterStringListViewModel(final boolean regex, final String content) {
        predicate.bind(Bindings.createObjectBinding(
                this::tryCreatePredicate,
                this.regex,
                this.content
        ));
        valid.bind(predicate.isNotNull());

        setRegex(regex);
        setContent(content);
    }

    private Filter.StringPredicate tryCreatePredicate() {
        if (content.get() == null) {
            return null;
        }

        return Filter.StringPredicate.tryCreate(this.content.get(), this.regex.get()).orElse(null);
    }

    public boolean isRegex() {
        return regex.get();
    }

    public BooleanProperty regexProperty() {
        return regex;
    }

    public void setRegex(final boolean regex) {
        this.regex.set(regex);
    }

    public String getContent() {
        return content.get();
    }

    public StringProperty contentProperty() {
        return content;
    }

    public void setContent(final String content) {
        this.content.set(content);
    }

    public Filter.StringPredicate getPredicate() {
        return predicate.get();
    }

    public ReadOnlyObjectProperty<Filter.StringPredicate> predicateProperty() {
        return predicate.getReadOnlyProperty();
    }

    public boolean isValid() {
        return valid.get();
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }
}
