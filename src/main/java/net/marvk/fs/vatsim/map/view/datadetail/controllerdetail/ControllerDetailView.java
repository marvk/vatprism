package net.marvk.fs.vatsim.map.view.datadetail.controllerdetail;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.IcaoVisitor;
import net.marvk.fs.vatsim.map.view.datadetail.NameVisitor;
import net.marvk.fs.vatsim.map.view.datadetail.clientdetail.ClientDetailView;
import net.marvk.fs.vatsim.map.view.detailsubview.DataDetailSubView;

import java.util.List;

public class ControllerDetailView extends DataDetailSubView<ControllerDetailViewModel, Controller> {
    @FXML
    private Label controllingDescription;
    @FXML
    private Pane typeBackground;
    @FXML
    private Label type;
    @FXML
    private Label controlling;
    @FXML
    private Label frequency;
    @FXML
    private Label rating;
    @FXML
    private TextArea atis;

    @FXML
    private ClientDetailView clientController;

    private final NameVisitor nameVisitor = new NameVisitor();
    private final IcaoVisitor icaoVisitor = new IcaoVisitor("????");

    @Override
    protected List<TextArea> textAreas() {
        return List.of(
                atis
        );
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                type,
                controlling,
                frequency,
                rating
        );
    }

    @Override
    protected void setData(final Controller controller) {
        clientController.getViewModel().setData(controller);
        type.setText(controller.getControllerType().toString());
        frequency.textProperty().bind(controller.frequencyProperty());
        rating.textProperty().bind(controller.ratingProperty());
        atis.textProperty().bind(controller.atisMessageProperty());

        type.setStyle("-fx-text-fill: #" + webColor(color("airports.type_label_color")));
        typeBackground.setStyle("-fx-background-color: #" + webColor(color(colorKey(controller))));

        controlling.setText(icaoVisitor.visit(controller.getWorkingArea()));
        controllingDescription.setText(nameVisitor.visit(controller.getWorkingArea()));
        controlling.setOnMouseClicked(event -> viewModel.setDataDetail(controller.getWorkingArea()));
    }

    private Color color(final String key) {
        if (key == null) {
            return Color.TRANSPARENT;
        }

        return viewModel.getPreferences().colorProperty(key).get();
    }

    @Override
    protected void clear(final Controller oldValue) {
        super.clear(oldValue);
        clientController.getViewModel().setData(null);
    }
}
