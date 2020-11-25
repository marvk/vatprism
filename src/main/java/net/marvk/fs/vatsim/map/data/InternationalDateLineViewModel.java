package net.marvk.fs.vatsim.map.data;

import com.google.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.marvk.fs.vatsim.api.data.Line;

public class InternationalDateLineViewModel extends SimpleDataViewModel<Line, InternationalDateLineViewModel> {
    private final ObjectProperty<Polygon> polygon = new SimpleObjectProperty<>();

    @Inject
    public InternationalDateLineViewModel() {
        setupBindings();
    }

    private void setupBindings() {
        modelProperty().addListener((observable, oldValue, newValue) -> polygon.set(new Polygon(newValue.getPoints())));
    }

    public Polygon polygon() {
        return polygon.get();
    }
}
