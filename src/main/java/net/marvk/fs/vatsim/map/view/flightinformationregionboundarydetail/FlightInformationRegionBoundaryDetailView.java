package net.marvk.fs.vatsim.map.view.flightinformationregionboundarydetail;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import net.marvk.fs.vatsim.map.data.FlightInformationRegion;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailSubViewModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FlightInformationRegionBoundaryDetailView extends DataDetailSubView<DataDetailSubViewModel<FlightInformationRegionBoundary>, FlightInformationRegionBoundary> {
    @FXML
    private Label name;
    @FXML
    private Label position;

    @Override
    public void initialize() {
        super.initialize();
        name.setWrapText(true);
    }

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                name,
                position
        );
    }

    @Override
    protected void setData(final FlightInformationRegionBoundary data) {
        final String names = data
                .getFlightInformationRegions()
                .stream()
                .map(FlightInformationRegion::getName)
                .collect(Collectors.joining("\n"));
        name.setText(names);
        position.setText(positionLabel(data.getPolygon().getPolyLabel()));
    }
}
