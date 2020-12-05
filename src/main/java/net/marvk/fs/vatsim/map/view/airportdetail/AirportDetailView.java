package net.marvk.fs.vatsim.map.view.airportdetail;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.view.BindingsUtil;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;

import java.util.Collections;
import java.util.List;

public class AirportDetailView extends DataDetailSubView<DataDetailSubViewModel<Airport>, Airport> {
    @FXML
    private Label position;
    @FXML
    private Label name;

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(position, name);
    }

    @Override
    protected void setData(final Airport airport) {
        name.setText(airport.getNames().get(0));
        position.textProperty().bind(BindingsUtil.position(airport.positionProperty()));
    }
}
