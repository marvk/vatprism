package net.marvk.fs.vatsim.map.view.datadetail.flightinformationregionboundarydetail;

import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.data.FlightInformationRegion;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.controllersdetail.ControllersDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class FlightInformationRegionBoundaryDetailView extends DataDetailSubView<DataDetailSubViewModel<FlightInformationRegionBoundary>, FlightInformationRegionBoundary> {
    @FXML
    private Label country;
    @FXML
    private DataDetailPane status;
    @FXML
    private DataDetailPane uirsContainer;
    @FXML
    private Label name;
    @FXML
    private Label position;
    @FXML
    private VBox container;
    @FXML
    private GridPane uirsGrid;
    @FXML
    private ControllersDetailView controllersController;

    @Override
    protected List<Label> labels() {
        return List.of(
                name,
                position,
                country
        );
    }

    @Override
    protected List<StringProperty> stringProperties() {
        return List.of(status.headerTextProperty());
    }

    private void uirsChanged(final ListChangeListener.Change<? extends UpperInformationRegion> c) {
        final ObservableList<UpperInformationRegion> uirs = viewModel.getData().getUpperInformationRegions();
        setUirPanes(uirs.isEmpty());

        uirsGrid.getChildren().clear();

        for (int i = 0; i < uirs.size(); i++) {
            final UpperInformationRegion uir = uirs.get(i);
            final Label icaoLabel = icaoLabel(uir);
            uirsGrid.add(icaoLabel, 0, i);
            final Label nameLabel = new Label(uir.getName());
            uirsGrid.add(nameLabel, 1, i);
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
        position.setText(positionLabel(firb.getPolygon().getExteriorRing().getPolyLabel()));
        status.setHeaderText(firb.getIcao());
        firb.getUpperInformationRegions().addListener(this::uirsChanged);
        uirsChanged(null);
        controllersController.getViewModel().setData(firb.getControllers());

        if (firb.getCountry() == null) {
            country.setText("UNKNOWN");
        } else {
            country.setText(firb.getCountry().getName());
        }
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
