package net.marvk.fs.vatsim.map.data;

import de.saxsys.mvvmfx.utils.mapping.ModelWrapper;
import de.saxsys.mvvmfx.utils.mapping.accessorfunctions.BooleanGetter;
import de.saxsys.mvvmfx.utils.mapping.accessorfunctions.DoubleGetter;
import de.saxsys.mvvmfx.utils.mapping.accessorfunctions.ObjectGetter;
import de.saxsys.mvvmfx.utils.mapping.accessorfunctions.StringGetter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import java.util.function.Function;

public abstract class SimpleDataViewModel<Model, ViewModel extends SimpleDataViewModel<Model, ViewModel>> implements DataViewModel<Model, ViewModel> {
    protected final ModelWrapper<Model> wrapper = new ModelWrapper<>() {
        @Override
        public void reload() {
            super.reload();
            if (super.modelProperty().get() == null) {
                reset();
            }
        }
    };

    public SimpleDataViewModel(final ViewModel viewModel) {
        this(viewModel != null ? viewModel.getModel() : null);
    }

    public SimpleDataViewModel() {
        this((Model) null);
    }

    public SimpleDataViewModel(final Model model) {
        setModel(model);
    }

    @Override
    public Model getModel() {
        return wrapper.modelProperty().get();
    }

    @Override
    public void setModel(final Model model) {
        wrapper.modelProperty().set(model);
    }

    @Override
    public void setModelFromViewModel(final ViewModel viewModel) {
        if (viewModel == null) {
            setModel(null);
        } else {
            setModel(viewModel.getModel());
        }
    }

    @Override
    public ObjectProperty<Model> modelProperty() {
        return wrapper.modelProperty();
    }

    @Override
    public void clear() {
        wrapper.reset();
    }

    protected StringProperty stringProperty(final String identifier, final StringGetter<Model> mapper) {
        return wrapper.field(identifier, mapper, null);
    }

    protected DoubleProperty doubleProperty(final String identifier, final DoubleGetter<Model> mapper) {
        return wrapper.field(identifier, mapper, null);
    }

    protected <T> ObjectProperty<T> objectProperty(final String identifier, final ObjectGetter<Model, T> mapper) {
        return wrapper.field(identifier, mapper, null);
    }

    protected ObjectProperty<Point> pointProperty(final String identifier, final Function<Model, net.marvk.fs.vatsim.api.data.Point> position) {
        return objectProperty(
                identifier,
                a -> new Point(position.apply(a).getLongitude(), position.apply(a).getLatitude())
        );
    }

    protected BooleanProperty booleanProperty(final String identifier, final BooleanGetter<Model> mapper) {
        return wrapper.field(identifier, mapper, null);
    }
}
