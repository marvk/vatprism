package net.marvk.fs.vatsim.map.view.datadetail;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.Data;
import net.marvk.fs.vatsim.map.view.BindingsUtil;

import java.util.List;

public abstract class DataDetailSubView<DataDetailViewModel extends DataDetailSubViewModel<ViewModel>, ViewModel extends Data> implements FxmlView<DataDetailViewModel> {
    @InjectViewModel
    protected DataDetailViewModel viewModel;

    private List<Label> labels;
    private List<TextArea> textAreas;

    public DataDetailViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void goTo() {
        viewModel.goTo();
    }

    protected static StringBinding positionLabel(final ObservableObjectValue<Point2D> position) {
        return BindingsUtil.position(position);
    }

    protected static String positionLabel(final Point2D position) {
        return GeomUtil.format(position);
    }

    public void initialize() {
        viewModel.dataProperty().addListener((observable, oldValue, newValue) -> setDataNullable(newValue));
    }

    private void setDataNullable(final ViewModel data) {
        if (data == null) {
            clear();
        } else {
            setData(data);
        }
    }

    protected void clear() {
        for (final Label label : getLabels()) {
            label.textProperty().unbind();
            label.setText("");
        }

        for (final TextArea textArea : getTextAreas()) {
            textArea.textProperty().unbind();
            textArea.setText("");
        }
    }

    private List<Label> getLabels() {
        if (labels == null) {
            labels = labels();
        }

        return labels;
    }

    private List<TextArea> getTextAreas() {
        if (textAreas == null) {
            textAreas = textAreas();
        }

        return textAreas;
    }

    protected abstract List<TextArea> textAreas();

    protected abstract List<Label> labels();

    protected abstract void setData(final ViewModel data);
}
