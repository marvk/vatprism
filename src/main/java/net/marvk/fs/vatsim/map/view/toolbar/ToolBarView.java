package net.marvk.fs.vatsim.map.view.toolbar;

import com.google.inject.Inject;
import com.sun.javafx.css.PseudoClassState;
import de.saxsys.mvvmfx.*;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.view.about.AboutView;
import net.marvk.fs.vatsim.map.view.preferences.PreferencesView;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

public class ToolBarView implements FxmlView<ToolBarViewModel> {
    private final PreferencesView preferencesView;
    @FXML
    private Button reload;
    @FXML
    private ToggleButton autoReload;
    @FXML
    private ToggleButton fullScreen;
    @FXML
    private FontIcon fullScreenIcon;
    @FXML
    private HBox container;
    @FXML
    private ToggleButton enablePilotCallsign;
    @FXML
    private ToggleButton enableDepartureArrivalLines;
    @FXML
    private ToggleButton enableDebug;
    @InjectViewModel
    private ToolBarViewModel viewModel;

    @InjectContext
    private Context context;

    @Inject
    public ToolBarView(final PreferencesView preferencesView) {
        this.preferencesView = preferencesView;
    }

    private RotateTransition reloadRotateTransition;

    public void initialize() {
        container.sceneProperty().addListener((observable, oldValue, newValue) -> setupWindowBindings(newValue));

        bindBooleanBidirectional(enableDebug, "metrics.enabled");
        bindBooleanBidirectional(enablePilotCallsign, "pilots.label");
        bindBooleanBidirectional(enableDepartureArrivalLines, "connections.enabled");

        booleanProperty("general.debug").addListener((observable, oldValue, newValue) -> {
            final ObservableList<Node> children = container.getChildren();
            if (newValue) {
                if (!children.contains(enableDebug)) {
                    children.add(children.size() - 1, enableDebug);
                }
            } else {
                children.remove(enableDebug);
            }
        });

        reload.disableProperty().bind(viewModel.reloadExecutableProperty().not());
        autoReload.disableProperty().bind(viewModel.reloadExecutableProperty().not());
        autoReload.selectedProperty().bindBidirectional(viewModel.autoReloadProperty());

        reloadRotateTransition = new RotateTransition();

        reloadRotateTransition = new RotateTransition(Duration.seconds(1), autoReload.getGraphic());
        reloadRotateTransition.setByAngle(-360);
        reloadRotateTransition.setCycleCount(Animation.INDEFINITE);
        reloadRotateTransition.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Animation.Status.STOPPED) {
                autoReload.getGraphic().setRotate(0);
            }
        });

        viewModel.errorMessageProperty().addListener(observable -> {
            final boolean active = viewModel.getErrorMessage() != null;
            final PseudoClass error = PseudoClassState.getPseudoClass("error");
            reload.getGraphic().pseudoClassStateChanged(error, active);
            autoReload.getGraphic().pseudoClassStateChanged(error, active);
        });

        viewModel.reloadExecutableProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                reloadRotateTransition.stop();
            } else {
                reloadRotateTransition.play();
            }
        });
    }

    private void setupWindowBindings(final Scene scene) {
        scene.windowProperty()
             .addListener((observable, oldValue, newValue) -> setupFullScreenBindings((Stage) newValue));
    }

    private void setupFullScreenBindings(final Stage stage) {
        final BooleanProperty fullScreenProperty = booleanProperty("general.fullscreen");
        stage.fullScreenProperty().addListener(
                (observable, oldValue, newValue) -> {
                    fullScreenProperty.set(newValue);
                }
        );

        fullScreenProperty.addListener((observable, oldValue, newValue) -> stage.setFullScreen(newValue));

        fullScreenIcon.iconCodeProperty().bind(Bindings.<Ikon>createObjectBinding(
                () -> ikon(fullScreenProperty.get()), fullScreenProperty
        ));

        bindBooleanBidirectional(fullScreen, fullScreenProperty);
    }

    private static Octicons ikon(final boolean fullscreen) {
        if (fullscreen) {
            return Octicons.SCREEN_NORMAL_16;
        }

        return Octicons.SCREEN_FULL_16;
    }

    private BooleanProperty booleanProperty(final String key) {
        return viewModel.getPreferences().booleanProperty(key);
    }

    private void bindBooleanBidirectional(final Toggle button, final String key) {
        bindBooleanBidirectional(button.selectedProperty(), key);
    }

    private void bindBooleanBidirectional(final Property<Boolean> property, final String key) {
        bindBooleanBidirectional(property, booleanProperty(key));
    }

    private static void bindBooleanBidirectional(final Toggle button, final Property<Boolean> property) {
        bindBooleanBidirectional(button.selectedProperty(), property);
    }

    private static void bindBooleanBidirectional(final Property<Boolean> property1, final Property<Boolean> property2) {
        property1.bindBidirectional(property2);
    }

    @FXML
    private void refresh(final ActionEvent actionEvent) {
        viewModel.refresh();
    }

    @SneakyThrows
    @FXML
    private void openSettings(final ActionEvent actionEvent) {
        preferencesView.show();
    }

    @FXML
    private void showAbout(final ActionEvent actionEvent) {
        final Parent view = FluentViewLoader.fxmlView(AboutView.class).load().getView();

        final Popup popup = new Popup();
        popup.getContent().add(view);
        popup.setAutoHide(true);
        popup.show(container.getScene().getWindow());
    }
}
