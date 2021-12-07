package net.marvk.fs.vatsim.map.view.datadetail.controllerdetail;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Atis;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerRating;
import net.marvk.fs.vatsim.map.data.IcaoVisitor;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.NameVisitor;
import net.marvk.fs.vatsim.map.view.datadetail.clientdetail.ClientDetailView;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubView;

import java.util.List;

public class ControllerDetailView extends DataDetailSubView<ControllerDetailViewModel, Controller> {
    @FXML
    private DataDetailPane atisPane;
    @FXML
    private Label controllingDescription;
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
    private Pane typeBackground;

    @FXML
    private ClientDetailView clientController;

    private final IcaoVisitor icaoVisitor = new IcaoVisitor("????");
    private NameVisitor nameVisitor;

    @Override
    public void initialize() {
        super.initialize();
        nameVisitor = new NameVisitor(resourceBundle);
        atis.setOnMouseClicked(atisPane::fireEvent);
    }

    @Override
    protected List<TextArea> textAreas() {
        return List.of(
                atis
        );
    }

    @Override
    protected List<Label> labels() {
        return List.of(
                controllingDescription,
                type,
                controlling,
                frequency,
                rating
        );
    }

    @Override
    protected List<StringProperty> stringProperties() {
        return List.of(atisPane.headerTextProperty());
    }

    @Override
    protected void setData(final Controller controller) {
        clientController.getViewModel().setData(controller);
        type.setText(controller.getControllerType().toString());
        frequency.textProperty().bind(controller.frequencyProperty());
        rating.textProperty().bind(Bindings.createStringBinding(() -> {
            final ControllerRating r = controller.ratingProperty().get();
            if (r == null) {
                return resourceBundle.getString("detail.controller.unknown");
            }
            return "%s (%s)".formatted(r.getLongName(), r.getShortName());
        }, controller.ratingProperty()));
        atis.textProperty().bind(Bindings.createStringBinding(() -> {
                    final String msg = controller.getAtisMessage();
                    if (msg == null || msg.isEmpty()) {
                        return resourceBundle.getString("detail.controller.no_atis");
                    }

                    return msg;
                }, controller.atisMessageProperty()
        ));

        type.setStyle("-fx-text-fill: #" + webColor(color("airports.controller_label_color")));
        typeBackground.setStyle("-fx-background-color: #" + webColor(color(colorKey(controller))));

        controlling.setText(icaoVisitor.visit(controller.getWorkingLocation()));
        controllingDescription.setText(nameVisitor.visit(controller.getWorkingLocation()));
        controlling.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                viewModel.setDataDetail(controller.getWorkingLocation());
                e.consume();
            }
        });

        setAtisHeaderBindings(controller);
    }

    private void setAtisHeaderBindings(final Controller controller) {
        final StringProperty atisHeaderProperty = atisPane.headerTextProperty();

        final String atisString = resourceBundle.getString("detail.controller.atis");

        if (controller instanceof Atis) {
            final ReadOnlyStringProperty atisProperty = ((Atis) controller).atisCodeProperty();
            atisHeaderProperty.bind(Bindings.createStringBinding(
                    () -> {
                        if (atisProperty.get() != null) {
                            return "%s (%s)".formatted(atisString, atisProperty.get());
                        }

                        return atisString;
                    },
                    atisProperty
            ));
        } else {
            atisHeaderProperty.unbind();
            atisHeaderProperty.set(atisString);
        }
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
        setAtisHeaderBindings(null);
    }
}
