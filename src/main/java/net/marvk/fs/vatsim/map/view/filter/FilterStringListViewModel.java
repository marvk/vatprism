package net.marvk.fs.vatsim.map.view.filter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import net.marvk.fs.vatsim.map.data.Filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FilterStringListViewModel {
    private final ReadOnlyObjectWrapper<Filter.StringPredicate> predicate = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper();

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty regex = new SimpleBooleanProperty();
    private final StringProperty content = new SimpleStringProperty();

    public FilterStringListViewModel(@Nullable final String name, @Nonnull final String content, final boolean regex) {
        predicate.bind(Bindings.createObjectBinding(
                this::tryCreatePredicate,
                this.regex,
                this.content,
                this.name
        ));
        valid.bind(predicate.isNotNull());

        setName(name);
        setRegex(regex);
        setContent(content);
    }

    private Filter.StringPredicate tryCreatePredicate() {
        if (content.get() == null) {
            return null;
        }

        return Filter.StringPredicate.tryCreate(this.name.get(), this.content.get(), this.regex.get()).orElse(null);
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

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(final String name) {
        this.name.set(name);
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

    @Override
    public String toString() {
        return "FilterStringListViewModel{" +
                "name=" + name.getValue() +
                ", regex=" + regex.getValue() +
                ", content=" + content.getValue() +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final FilterStringListViewModel that = (FilterStringListViewModel) o;

        if (name.getValue() != null ? !name.getValue().equals(that.name.getValue()) : that.name.getValue() != null) {
            return false;
        }
        if (regex.getValue() != null ? !regex.getValue().equals(that.regex.getValue()) : that.regex.getValue() != null) {
            return false;
        }
        return content.getValue() != null ? content.getValue().equals(that.content.getValue()) : that.content.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = name.getValue() != null ? name.hashCode() : 0;
        result = 31 * result + (regex.getValue() != null ? regex.hashCode() : 0);
        result = 31 * result + (content.getValue() != null ? content.hashCode() : 0);
        return result;
    }
}
