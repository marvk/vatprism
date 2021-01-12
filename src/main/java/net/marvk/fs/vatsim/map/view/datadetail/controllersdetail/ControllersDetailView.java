package net.marvk.fs.vatsim.map.view.datadetail.controllersdetail;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubView;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class ControllersDetailView extends DetailSubView<ControllersDetailViewModel, ObservableList<Controller>> {
    @FXML
    private HBox noControllers;
    @FXML
    private VBox contentContainer;
    @FXML
    private GridPane controllersGrid;

    private void setControllerPanes(final Boolean controllers) {
        final ObservableList<Node> children = contentContainer.getChildren();

        if (controllers) {
            children.setAll(controllersGrid);
        } else {
            children.setAll(noControllers);
        }
    }

    private Color color(final String key) {
        final Color color = viewModel.getPreferences().colorProperty(key).get();
        if (color == null) {
            log.error("Unable to find color with key \"%s\"".formatted(key));
        }
        return color;
    }

    @Override
    protected void clear(final ObservableList<Controller> oldValue) {
        super.clear(oldValue);
        oldValue.removeListener(this::onDataChanged);
        setControllerPanes(false);
        controllersGrid.getChildren().clear();
    }

    @Override
    protected void setData(final ObservableList<Controller> data) {
        data.addListener(this::onDataChanged);
        onDataChanged(null);
    }

    protected void onDataChanged(final ListChangeListener.Change<? extends Controller> change) {
        controllersGrid.getChildren().clear();
        final List<Controller> controllers = viewModel
                .getData()
                .stream()
                .sorted((o1, o2) -> ControllerType.COMPARATOR.compare(o1.getControllerType(), o2.getControllerType()))
                .collect(Collectors.toList());

        setControllerPanes(!controllers.isEmpty());

        for (int i = 0; i < controllers.size(); i++) {
            final Controller controller = controllers.get(i);

            final String typeLabel = controller.getControllerType().toString();
            final Label type = new Label(" ".repeat(4 - typeLabel.length()) + typeLabel);
            type.setPadding(new Insets(0, 2, 0, 2));
            type.setStyle("-fx-text-fill: #" + webColor(color("airports.controller_label_color")));
            type.getStyleClass().add("mono");
            final Pane typeHolder = new Pane(type);
            typeHolder.setStyle("-fx-background-color: #" + webColor(color(colorKey(controller))));
            controllersGrid.add(typeHolder, 0, i);

            final Label frequency = new Label(controller.getFrequency());
            frequency.getStyleClass().add("mono");
            controllersGrid.add(frequency, 1, i);

            final Label callsign = new Label(controller.getCallsign());
            callsign.getStyleClass().add("mono");
            callsign.getStyleClass().add("hyperlink-label");
            controllersGrid.add(callsign, 2, i);
            callsign.setOnMouseClicked(e -> viewModel.setDataDetail(controller));

            final Label name = new Label(controller.getRealName());
            controllersGrid.add(name, 3, i);

            final Label onlineFor = new Label(onlineForString(controller.getLogonTime()));
            onlineFor.getStyleClass().add("mono");
            controllersGrid.add(onlineFor, 4, i);
        }
    }
}
