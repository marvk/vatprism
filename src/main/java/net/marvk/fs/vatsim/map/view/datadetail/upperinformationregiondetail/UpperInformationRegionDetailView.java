package net.marvk.fs.vatsim.map.view.datadetail.upperinformationregiondetail;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import net.marvk.fs.vatsim.map.GeomUtil;
import net.marvk.fs.vatsim.map.data.FlightInformationRegionBoundary;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.controllersdetail.ControllersDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

import java.util.Collections;
import java.util.List;

public class UpperInformationRegionDetailView extends DataDetailSubView<DataDetailSubViewModel<UpperInformationRegion>, UpperInformationRegion> {
    @FXML
    private DataDetailPane status;
    @FXML
    private VBox container;
    @FXML
    private Label name;
    @FXML
    private Label position;
    @FXML
    private GridPane firsGrid;
    @FXML
    private DataDetailPane firsContainer;
    @FXML
    private ControllersDetailView controllersController;

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                name, position
        );
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

        for (int i = 0; i < firs.size(); i++) {
            final FlightInformationRegionBoundary fir = firs.get(i);
            final Label icaoLabel = new Label(fir.getIcao());
            icaoLabel.getStyleClass().add("mono");
            icaoLabel.getStyleClass().add("hyperlink-label");
            icaoLabel.setOnMouseClicked(e -> viewModel.setDataDetail(fir));
            firsGrid.add(icaoLabel, 0, i);
        }
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
