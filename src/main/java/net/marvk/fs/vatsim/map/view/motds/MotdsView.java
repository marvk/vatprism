package net.marvk.fs.vatsim.map.view.motds;

import com.sandec.mdfx.MarkdownView;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.util.Callback;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDay;

public class MotdsView implements FxmlView<MotdsViewModel> {
    @FXML
    private Pagination pagination;

    @InjectViewModel
    private MotdsViewModel viewModel;

    private final MarkdownView markdownView = new MarkdownView();
    private final ScrollPane scrollPane = new ScrollPane(markdownView);

    public void initialize() {
        pagination.setPrefWidth(1500);
        markdownView.setPrefWidth(1500);
        pagination.pageCountProperty().bind(viewModel.sizeProperty());
        pagination.currentPageIndexProperty().bindBidirectional(viewModel.selectedIndexProperty());
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(final Integer index) {
                final MessageOfTheDay messageOfTheDay = viewModel.getSelectedMessageOfTheDay();

                if (messageOfTheDay != null) {
                    markdownView.setMdString(messageOfTheDay.getContent());
                } else {
                    markdownView.setMdString("Empty");
                }

                return scrollPane;
            }
        });



    }
}
