package net.marvk.fs.vatsim.map.view.filter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FilterStringListViewModel {
    private final BooleanProperty regex = new SimpleBooleanProperty();
    private final StringProperty content = new SimpleStringProperty();

    public FilterStringListViewModel(final boolean regex, final String content) {
        setRegex(regex);
        setContent(content);
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
}
