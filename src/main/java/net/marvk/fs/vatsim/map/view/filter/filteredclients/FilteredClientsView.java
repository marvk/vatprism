package net.marvk.fs.vatsim.map.view.filter.filteredclients;

import de.saxsys.mvvmfx.*;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FilteredClientsView implements FxmlView<FilteredClientsViewModel> {
    @FXML
    private VBox container;
    @FXML
    private CheckBox controllers;
    @FXML
    private CheckBox pilots;

    @InjectViewModel
    private FilteredClientsViewModel viewModel;

    @InjectContext
    private Context context;

    public void initialize() {
        final var viewTuple = FluentViewLoader
                .javaView(FilteredClientsTableView.class)
                .context(context)
                .load();
        viewTuple.getViewModel().predicateProperty().bind(viewModel.predicateProperty());
        final Parent clientsTable = viewTuple.getView();
        VBox.setVgrow(clientsTable, Priority.ALWAYS);

        container.getChildren().add(clientsTable);
        viewModel.controllersProperty().bind(controllers.selectedProperty());
        viewModel.pilotsProperty().bind(pilots.selectedProperty());
    }
}
