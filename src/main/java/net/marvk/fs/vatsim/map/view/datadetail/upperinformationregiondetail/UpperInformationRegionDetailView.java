package net.marvk.fs.vatsim.map.view.datadetail.upperinformationregiondetail;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.ImmutableStringProperty;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.controllersdetail.ControllersDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;

import java.util.List;

public class UpperInformationRegionDetailView extends DataDetailSubView<UpperInformationRegionDetailViewModel, UpperInformationRegion> {
    private static final ReadOnlyStringProperty EMPTY = new ImmutableStringProperty(null);

    @FXML
    private DataDetailPane status;
    @FXML
    private DataDetailPane firsContainer;
    @FXML
    private Label name;
    @FXML
    private Label position;
    @FXML
    private VBox container;
    @FXML
    private GridPane firsGrid;
    @FXML
    private ControllersDetailView controllersController;

    @Override
    protected List<Label> labels() {
        return List.of(
                name, position
        );
    }

    @Override
    protected List<StringProperty> stringProperties() {
        return List.of(status.headerTextProperty());
    }

    @Override
    protected void setData(final UpperInformationRegion uir) {
        name.setText(uir.getName());
        status.setHeaderText(uir.getIcao());
        position.setText(positionLabel(GeomUtil.center(uir.getBounds())));

        controllersController.getViewModel().setData(uir.getControllers());

        uir.getFlightInformationRegionBoundaries().addListener(this::firsChanged);
        firsChanged(null);
    }

    private void firsChanged(final ListChangeListener.Change<? extends FlightInformationRegionBoundary> c) {
        final ObservableList<FlightInformationRegionBoundary> firs = viewModel.getData()
                                                                              .getFlightInformationRegionBoundaries();
        setFirPanes(firs.isEmpty());

        firsGrid.getChildren().clear();

        final int columnAfterCtrOffset = firs.stream().anyMatch(FlightInformationRegionBoundary::hasFirControllers)
                ? 0
                : -1;

        for (int i = 0; i < firs.size(); i++) {
            final FlightInformationRegionBoundary fir = firs.get(i);
            final Label icaoLabel = icaoLabel(fir);
            firsGrid.add(icaoLabel, 0, i);
            if (fir.hasFirControllers()) {
                final Pane pane = controllerTypePane(ControllerType.CTR.name(), viewModel.getLabelColor(), viewModel.getFirColor());
                firsGrid.add(pane, 1, i);
            }
            firsGrid.add(new Label(nameProperty(fir).get()), 2 + columnAfterCtrOffset, i);
            firsGrid.add(new Label(fir.getCountry().getName()), 3 + columnAfterCtrOffset, i);
        }
    }

    private static ReadOnlyStringProperty nameProperty(final FlightInformationRegionBoundary e) {
        if (e.getFlightInformationRegions().isEmpty()) {
            return EMPTY;
        }

        return e.getFlightInformationRegions().get(0).nameProperty();
    }

    private void setFirPanes(final boolean uirs) {
        if (uirs) {
            container.getChildren().remove(firsContainer);
        } else if (!container.getChildren().contains(firsContainer)) {
            container.getChildren().add(firsContainer);
        }
    }

    @Override
    protected void clear(final UpperInformationRegion oldValue) {
        super.clear(oldValue);
        if (oldValue != null) {
            controllersController.getViewModel().setData(null);
        }
    }
}
