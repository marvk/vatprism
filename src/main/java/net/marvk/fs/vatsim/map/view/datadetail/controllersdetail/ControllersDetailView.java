package net.marvk.fs.vatsim.map.view.datadetail.controllersdetail;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Controller;
import net.marvk.fs.vatsim.map.data.ControllerType;
import net.marvk.fs.vatsim.map.view.detailsubview.DetailSubView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ControllersDetailView extends DetailSubView<ControllersDetailViewModel, ObservableList<Controller>> {
    @FXML
    private HBox noControllers;
    @FXML
    private VBox container;
    @FXML
    private GridPane controllersGrid;

    private void setControllerPanes(final Boolean firs) {
        final ObservableList<Node> children = container.getChildren();
        children.setAll(children.get(0));

        if (firs) {
            children.add(controllersGrid);
        } else {
            children.add(noControllers);
        }
    }

    private Color color(final String key) {
        if (key == null) {
            return Color.HOTPINK;
        }

        return viewModel.getPreferences().colorProperty(key).get();
    }

    @Override
    protected List<TextArea> textAreas() {
        return Collections.emptyList();
    }

    @Override
    protected List<Label> labels() {
        return Collections.emptyList();
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

    protected void onDataChanged(ListChangeListener.Change<? extends Controller> change) {
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
            type.setStyle("-fx-text-fill: #" + webColor(color("airports.type_label_color")));
            type.getStyleClass().add("mono");
            final Pane typeHolder = new Pane(type);
            typeHolder.setStyle("-fx-background-color: #" + webColor(color(colorKey(controller))));
            controllersGrid.add(typeHolder, 0, i);
            GridPane.setHgrow(typeHolder, Priority.NEVER);

            final Label frequency = new Label(controller.getFrequency());
            frequency.getStyleClass().add("mono");
            controllersGrid.add(frequency, 1, i);
            GridPane.setHgrow(frequency, Priority.NEVER);

            final Label callsign = new Label(controller.getCallsign());
            callsign.getStyleClass().add("mono");
            callsign.getStyleClass().add("hyperlink-label");
            controllersGrid.add(callsign, 2, i);
            callsign.setOnMouseClicked(e -> viewModel.setDataDetail(controller));
            GridPane.setHgrow(callsign, Priority.NEVER);

            final Label onlineFor = new Label(onlineForString(controller.getLogonTime()));
            onlineFor.getStyleClass().add("mono");
            controllersGrid.add(onlineFor, 3, i);
            GridPane.setHgrow(onlineFor, Priority.ALWAYS);
        }
    }
}
