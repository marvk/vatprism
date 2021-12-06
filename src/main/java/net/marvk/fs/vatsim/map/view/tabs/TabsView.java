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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.view.Notifications;
import net.marvk.fs.vatsim.map.view.datatable.airportstable.AirportsTableView;
import net.marvk.fs.vatsim.map.view.datatable.clientstable.ClientsTableView;
import net.marvk.fs.vatsim.map.view.datatable.controllerstable.ControllersTableView;
import net.marvk.fs.vatsim.map.view.datatable.flightinformationregionboundariestable.FlightInformationRegionBoundariesTableView;
import net.marvk.fs.vatsim.map.view.datatable.pilotstable.PilotsTableView;
import net.marvk.fs.vatsim.map.view.datatable.streamerstable.StreamersTableView;
import net.marvk.fs.vatsim.map.view.datatable.upperinformationregionstable.UpperInformationRegionsTableView;
import net.marvk.fs.vatsim.map.view.filter.filteredclients.FilteredClientsView;
import net.marvk.fs.vatsim.map.view.filter.filteroutline.FilterOutlineView;
import net.marvk.fs.vatsim.map.view.map.MapView;
import net.marvk.fs.vatsim.map.view.search.SearchView;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.ResourceBundle;

public class TabsView implements FxmlView<TabsViewModel> {
    @FXML
    private StackPane tabPaneHolder;

    @FXML
    private SearchView searchController;

    @InjectViewModel
    private TabsViewModel viewModel;

    @InjectContext
    private Context context;

    @InjectResourceBundle
    private ResourceBundle resourceBundle;

    private TextField searchBox;

    public void initialize() {
        final Pane searchBoxContainer = searchController.getContainer();
        final Pane searchBoxHolder = searchController.getSearchBoxHolder();
        final TextField searchBox = searchController.getSearchBox();
        final CustomTabPane tabPane = new CustomTabPane(searchBoxHolder.widthProperty());

        searchBoxHolder.prefHeightProperty().bind(tabPane.headersRegion().heightProperty().subtract(1));
        searchBoxHolder.maxHeightProperty().bind(searchBoxHolder.prefHeightProperty());
        tabPaneHolder.getChildren().add(0, tabPane);
        StackPane.setAlignment(searchBoxContainer, Pos.TOP_LEFT);
        tabPane.getTabs().clear();
        tabPane.getTabs().add(createFxmlViewTab("tabs.map", MapView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.clients", ClientsTableView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.pilots", PilotsTableView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.controllers", ControllersTableView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.airports", AirportsTableView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.firs", FlightInformationRegionBoundariesTableView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.uirs", UpperInformationRegionsTableView.class));
        tabPane.getTabs().add(createFxmlViewTab("tabs.filtered_clients", FilteredClientsView.class));
        tabPane.getTabs().add(createFxmlViewTab("tabs.filters", FilterOutlineView.class));
        tabPane.getTabs().add(createJavaViewTab("tabs.streamers", StreamersTableView.class));

        searchController.resultsVisibleProperty().bind(Bindings.createBooleanBinding(
                () -> tabPane.getSelectionModel().getSelectedIndex() == 0,
                tabPane.getSelectionModel().selectedIndexProperty()
        ));

        Notifications.SWITCH_TO_TAB.subscribe(i -> tabPane.getSelectionModel().select(i));
    }

    private Tab createFxmlViewTab(final String key, final Class<? extends FxmlView<?>> clazz) {
        return new Tab(resourceBundle.getString(key), loadFxmlView(clazz));
    }

    private Tab createJavaViewTab(final String key, final Class<? extends JavaView<?>> clazz) {
        return new Tab(resourceBundle.getString(key), loadJavaView(clazz));
    }

    private Parent loadFxmlView(final Class<? extends FxmlView<?>> clazz) {
        return FluentViewLoader.fxmlView(clazz)
                               .context(context)
                               .load()
                               .getView();
    }

    private Parent loadJavaView(final Class<? extends JavaView<?>> clazz) {
        return FluentViewLoader.javaView(clazz)
                               .context(context)
                               .load()
                               .getView();
    }

    private static class CustomTabPane extends TabPane {
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
