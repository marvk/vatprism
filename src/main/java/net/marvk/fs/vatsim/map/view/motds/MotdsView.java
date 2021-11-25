package net.marvk.fs.vatsim.map.view.motds;

import com.sandec.mdfx.MarkdownView;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDay;

public class MotdsView implements FxmlView<MotdsViewModel> {
    @FXML
    private Pagination pagination;

    @InjectViewModel
    private MotdsViewModel viewModel;

    private final MarkdownView markdownView = new MarkdownView();
    private final ScrollPane scrollPane = new ScrollPane(markdownView);

    public void initialize() {
        scrollPane.setFitToWidth(true);
        markdownView.mdStringProperty().bind(Bindings.createStringBinding(() -> {
            final MessageOfTheDay motd = viewModel.getSelectedMessageOfTheDay();
            return motd == null ? "Empty" : motd.getContent();
        }, viewModel.selectedMessageOfTheDayProperty()));
        markdownView.setPadding(new Insets(0, 50, 0, 50));
        pagination.pageCountProperty().bind(viewModel.sizeProperty());
        pagination.currentPageIndexProperty().bindBidirectional(viewModel.selectedIndexProperty());
        pagination.setPageFactory(index -> scrollPane);

    }
}
