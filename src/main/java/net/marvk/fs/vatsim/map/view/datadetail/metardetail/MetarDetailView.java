package net.marvk.fs.vatsim.map.view.datadetail.metardetail;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import net.marvk.fs.vatsim.map.data.Metar;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubView;

import java.util.List;

public class MetarDetailView extends DetailSubView<MetarDetailViewModel, Metar> {
    @FXML
    private Button refreshMetar;
    @FXML
    private TextArea metarText;
    @FXML
    private DataDetailPane container;

    @Override
    public void initialize() {
        super.initialize();
        refreshMetar.disableProperty().bind(viewModel.fetchMetar().notExecutableProperty());
        refreshMetar.setOnAction(e -> viewModel.fetchMetar().execute());
        metarText.setOnMouseClicked(container::fireEvent);
    }

    @Override
    protected List<TextArea> textAreas() {
        return List.of(metarText);
    }

    @Override
    protected void setData(final Metar data) {
        metarText.setText(data.getMetar());
    }

}
