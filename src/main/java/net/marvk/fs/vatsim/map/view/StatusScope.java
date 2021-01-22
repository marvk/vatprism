package net.marvk.fs.vatsim.map.view;

import com.google.inject.Singleton;
import de.saxsys.mvvmfx.Scope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import net.marvk.fs.vatsim.map.data.Data;

@Singleton
public class StatusScope implements Scope {
    private final ObjectProperty<Point2D> mouseWorldPosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Point2D> mouseViewPosition = new SimpleObjectProperty<>();
    private final ObservableList<Data> searchedData = FXCollections.observableArrayList();
    private final StringProperty searchQuery = new SimpleStringProperty();

    private final ObservableList<? extends Data> highlightedData = FXCollections.observableArrayList();

    public ObservableList<? extends Data> highlightedFirs() {
        return highlightedData;
    }

    public Point2D getMouseWorldPosition() {
        return mouseWorldPosition.get();
    }

    public ObjectProperty<Point2D> mouseWorldPositionProperty() {
        return mouseWorldPosition;
    }

    public void setMouseWorldPosition(final Point2D mouseWorldPosition) {
        this.mouseWorldPosition.set(mouseWorldPosition);
    }

    public Point2D getMouseViewPosition() {
        return mouseViewPosition.get();
    }

    public ObjectProperty<Point2D> mouseViewPositionProperty() {
        return mouseViewPosition;
    }

    public void setMouseViewPosition(final Point2D mouseViewPosition) {
        this.mouseViewPosition.set(mouseViewPosition);
    }

    public ObservableList<Data> getSearchedData() {
        return searchedData;
    }

    public String getSearchQuery() {
        return searchQuery.get();
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public void setSearchQuery(final String searchQuery) {
        this.searchQuery.set(searchQuery);
    }
}
