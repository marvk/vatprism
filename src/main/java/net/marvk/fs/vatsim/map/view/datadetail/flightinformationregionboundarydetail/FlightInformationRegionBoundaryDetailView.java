package net.marvk.fs.vatsim.map.view.datadetail.flightinformationregionboundarydetail;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.FlightInformationRegion;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.datadetail.controllersdetail.ControllersDetailView;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubViewModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FlightInformationRegionBoundaryDetailView extends DataDetailSubView<DataDetailSubViewModel<FlightInformationRegionBoundary>, FlightInformationRegionBoundary> {
    @FXML
    private VBox container;
    @FXML
    private VBox uirsContainer;
    @FXML
    private GridPane uirsGrid;
    @FXML
    private Label icao;
    @FXML
    private Label name;
    @FXML
    private Label position;
    @FXML
    private ControllersDetailView controllersController;

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
                position,
                icao
        );
    }

    private void uirsChanged(final ListChangeListener.Change<? extends UpperInformationRegion> c) {
        final ObservableList<UpperInformationRegion> uirs = viewModel.getData().getUpperInformationRegions();
        setUirPanes(uirs.isEmpty());

        uirsGrid.getChildren().clear();

        for (int i = 0; i < uirs.size(); i++) {
            final UpperInformationRegion uir = uirs.get(i);
            final Label icaoLabel = new Label(uir.getIcao());
            icaoLabel.getStyleClass().add("mono");
            icaoLabel.getStyleClass().add("hyperlink-label");
            icaoLabel.setOnMouseClicked(e -> viewModel.setDataDetail(uir));
            uirsGrid.add(icaoLabel, 0, i);
        }
    }

    private void setUirPanes(final boolean uirs) {
        if (uirs) {
            container.getChildren().remove(uirsContainer);
        } else if (!container.getChildren().contains(uirsContainer)) {
            container.getChildren().add(uirsContainer);
        }
    }

    @Override
    protected void setData(final FlightInformationRegionBoundary firb) {
        final String names = firb
                .getFlightInformationRegions()
                .stream()
                .map(FlightInformationRegion::getName)
                .distinct()
                .sorted()
                .collect(Collectors.joining("\n"));
        name.setText(names);
        position.setText(positionLabel(firb.getPolygon().getPolyLabel()));
        icao.setText(firb.getIcao());
        firb.getUpperInformationRegions().addListener(this::uirsChanged);
        uirsChanged(null);
        controllersController.getViewModel().setData(firb.getControllers());
    }

    @Override
    protected void clear(final FlightInformationRegionBoundary oldValue) {
        super.clear(oldValue);
        if (oldValue != null) {
            oldValue.getUpperInformationRegions().removeListener(this::uirsChanged);
            controllersController.getViewModel().setData(null);
        }
    }
}
