package net.marvk.fs.vatsim.map.view.datadetail.metardetail;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import net.marvk.fs.vatsim.map.data.Metar;
import net.marvk.fs.vatsim.map.view.datadetail.DataDetailPane;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DetailSubView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignR;

import java.util.List;

public class MetarDetailView extends DetailSubView<MetarDetailViewModel, Metar> {
    @FXML
    private Button refreshMetar;
    @FXML
    private TextArea metarText;
    @FXML
    private DataDetailPane container;
    private RotateTransition rotateTransition;

    @Override
    public void initialize() {
        super.initialize();
        rotateTransition = rotateTransition();

        refreshMetar.disableProperty().bind(viewModel.fetchMetar().notExecutableProperty());
        refreshMetar.setOnAction(e -> viewModel.fetchMetar().execute());
        metarText.setOnMouseClicked(container::fireEvent);
//        metarText.maxWidthProperty().bind(Bindings.createDoubleBinding(
//                () -> viewModel.getData() == null ? 0.0 : Double.POSITIVE_INFINITY,
//                viewModel.dataProperty()
//        ));
        viewModel.dataProperty().addListener((observable, oldValue, newValue) ->
                HBox.setHgrow(refreshMetar, newValue == null ? Priority.ALWAYS : Priority.NEVER)
        );
        final FontIcon icon = (FontIcon) refreshMetar.getGraphic();
        icon.iconCodeProperty().bind(Bindings.createObjectBinding(
                () -> viewModel.fetchMetar().isRunning() ? MaterialDesignD.DOWNLOAD : MaterialDesignR.RELOAD,
                viewModel.fetchMetar().runningProperty()
        ));
        viewModel.fetchMetar().runningProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rotateTransition.play();
            } else {
                rotateTransition.stop();
            }
        });
    }

    private RotateTransition rotateTransition() {
        final RotateTransition reloadRotateTransition = new RotateTransition(Duration.seconds(1), refreshMetar.getGraphic());
        reloadRotateTransition.setByAngle(-360);
        reloadRotateTransition.setCycleCount(Animation.INDEFINITE);
        reloadRotateTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
                refreshMetar.getGraphic().setRotate(0);
            }
        });
        return reloadRotateTransition;
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
