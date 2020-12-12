package net.marvk.fs.vatsim.map.view.tabs;

import de.saxsys.mvvmfx.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.StackPane;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.view.airports.AirportsView;
import net.marvk.fs.vatsim.map.view.clients.ClientsView;
import net.marvk.fs.vatsim.map.view.map.MapView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.octicons.Octicons;

import java.lang.reflect.Field;
import java.util.Arrays;

public class TabsView implements FxmlView<TabsViewModel> {
    @FXML
    private StackPane tabPaneHolder;

    @InjectViewModel
    private TabsViewModel viewModel;

    @InjectContext
    private Context context;
    private TextField searchBox;

    public void initialize() {
        final StackPane searchBoxHolder = new StackPane();
        StackPane.setAlignment(searchBoxHolder, Pos.TOP_LEFT);
        searchBoxHolder.getStyleClass().add("search-box");

        searchBox = new TextField();
        StackPane.setAlignment(searchBox, Pos.TOP_LEFT);
        searchBox.setPromptText("Search");
        searchBox.getStyleClass().add("search-box-text-field");

        final FontIcon icon = FontIcon.of(Octicons.SEARCH_16);
        StackPane.setMargin(icon, new Insets(0, 3, 0, 0));
        StackPane.setAlignment(icon, Pos.CENTER_RIGHT);

        searchBoxHolder.getChildren().addAll(searchBox, icon);
        final CustomTabPane tabPane = new CustomTabPane(searchBoxHolder.widthProperty());
        searchBoxHolder.prefHeightProperty().bind(tabPane.headersRegion().heightProperty().subtract(1));
        searchBoxHolder.maxHeightProperty().bind(searchBoxHolder.prefHeightProperty());
        tabPaneHolder.getChildren().addAll(tabPane, searchBoxHolder);
        tabPane.getTabs().clear();
        tabPane.getTabs().add(createTab("Map", MapView.class));
        tabPane.getTabs().add(createTab("Clients", ClientsView.class));
        tabPane.getTabs().add(createTab("Airports", AirportsView.class));

        tabPane.layout();
    }

    private Tab createTab(final String map, final Class<? extends FxmlView<?>> clazz) {
        return new Tab(map, loadView(clazz));
    }

    private Parent loadView(final Class<? extends FxmlView<?>> clazz) {
        return FluentViewLoader.fxmlView(clazz)
                               .context(context)
                               .load()
                               .getView();
    }

    private static class SearchBoxHolderTab extends Tab {
        public SearchBoxHolderTab(final ReadOnlyDoubleProperty height) {
            final TextField textField = new TextField();
            textField.getStyleClass().add("search-box-text-field");
            textField.setPrefWidth(200);
            textField.setPromptText("Search...");
            setGraphic(textField);
            setDisable(true);
            getStyleClass().add("search-box-holder-tab");
            textField.prefHeightProperty().bind(height);
        }
    }

    private static class CustomTabPane extends TabPane {
//        private final StackPane headerBackground;
//        private final StackPane headersRegion;
//        private final StackPane tabHeaderArea;

        @SneakyThrows
        public CustomTabPane(final ReadOnlyDoubleProperty paddingLeft) {
            setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
            setSkin(createDefaultSkin());

            tabHeaderArea().paddingProperty().bind(Bindings.createObjectBinding(
                    () -> new Insets(0, 0, 0, paddingLeft.doubleValue() + 1),
                    paddingLeft
            ));
        }

        private static Class<?> tabHeaderAreaClass() {
            return Arrays
                    .stream(TabPaneSkin.class.getDeclaredClasses())
                    .filter(e -> "TabHeaderArea".equals(e.getSimpleName()))
                    .findFirst()
                    .get();
        }

        @SneakyThrows
        private StackPane tabHeaderArea() {
            final Field tabHeaderArea = TabPaneSkin.class.getDeclaredField("tabHeaderArea");
            tabHeaderArea.setAccessible(true);
            return (StackPane) tabHeaderArea.get(getSkinImpl());
        }

        @SneakyThrows
        private StackPane headersRegion() {
            return fieldFromTabHeaderArea("headersRegion");
        }

        @SneakyThrows
        private StackPane headerBackground() {
            return fieldFromTabHeaderArea("headerBackground");
        }

        @SneakyThrows
        private StackPane fieldFromTabHeaderArea(final String fieldName) {
            final Field field = tabHeaderAreaClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (StackPane) field.get(tabHeaderArea());
        }

        private TabPaneSkin getSkinImpl() {
            return (TabPaneSkin) getSkin();
        }
    }
}
