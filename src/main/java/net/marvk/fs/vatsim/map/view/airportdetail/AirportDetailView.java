package net.marvk.fs.vatsim.map.view.airportdetail;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import net.marvk.fs.vatsim.map.data.Airport;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.BindingsUtil;
import net.marvk.fs.vatsim.map.view.controllersdetail.ControllersDetailView;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubView;

import java.util.Collections;
import java.util.List;

public class AirportDetailView extends DataDetailSubView<AirportDetailViewModel, Airport> {
    private static final String HYPERLINK_LABEL = "hyperlink-label";

    @FXML
    private Label icao;
    @FXML
    private Label fir;
    @FXML
    private Label position;
    @FXML
    private Label name;

    @FXML
    private ControllersDetailView controllersController;

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(position, name, icao);
    }

    @Override
    protected void setData(final Airport airport) {
        name.setText(airport.getNames().get(0));
        position.textProperty().bind(BindingsUtil.position(airport.positionProperty()));
        icao.setText(airport.getIcao());
        final FlightInformationRegionBoundary firb = airport.getFlightInformationRegionBoundary();

        if (firb == null) {
            fir.getStyleClass().remove(HYPERLINK_LABEL);
            fir.setText(null);
        } else {
            fir.getStyleClass().add(HYPERLINK_LABEL);
            fir.setText(firb.getIcao());
        }

        controllersController.getViewModel().setData(airport.getControllers());
    }

    @Override
    protected void clear(final Airport oldValue) {
        super.clear(oldValue);
        if (oldValue != null) {
            controllersController.getViewModel().setData(null);
        }
    }

    @FXML
    private void setToFir(final MouseEvent event) {
        viewModel.setToFir();
    }
}
