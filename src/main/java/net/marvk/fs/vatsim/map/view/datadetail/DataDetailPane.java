package net.marvk.fs.vatsim.map.view.datadetail;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class DataDetailPane extends Control {
    private final StringProperty headerText = new SimpleStringProperty();
    private final ObjectProperty<Node> headerNode = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> contentNode = new SimpleObjectProperty<>();
    private final BooleanProperty headerMonospace = new SimpleBooleanProperty(false);

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DataDetailPaneSkin(this);
    }

    public String getHeaderText() {
        return headerText.get();
    }

    public StringProperty headerTextProperty() {
        return headerText;
    }

    public void setHeaderText(final String headerText) {
        this.headerText.set(headerText);
    }

    public Node getHeaderNode() {
        return headerNode.get();
    }

    public ObjectProperty<Node> headerNodeProperty() {
        return headerNode;
    }

    public void setHeaderNode(final Node headerNode) {
        this.headerNode.set(headerNode);
    }

    public Node getContentNode() {
        return contentNode.get();
    }

    public ObjectProperty<Node> contentNodeProperty() {
        return contentNode;
    }

    public void setContentNode(final Node contentNode) {
        this.contentNode.set(contentNode);
    }

    public boolean isHeaderMonospace() {
        return headerMonospace.get();
    }

    public BooleanProperty headerMonospaceProperty() {
        return headerMonospace;
    }

    public void setHeaderMonospace(final boolean headerMonospace) {
        this.headerMonospace.set(headerMonospace);
    }

    public static class DataDetailPaneSkin implements Skin<DataDetailPane> {
        private static final String MONO_CLASS = "mono";
        private static final String HEADER_PANE_STYLE_CLASS = "header-pane";
        private static final String HEADER_PANE_LABEL_STYLE_CLASS = "header-label";
        private DataDetailPane dataDetailPane;
        private VBox node;
        private StackPane headerHolder;

        public DataDetailPaneSkin(final DataDetailPane dataDetailPane) {
            this.dataDetailPane = dataDetailPane;
            createNode();

            dataDetailPane.headerNodeProperty()
                          .addListener((observable, oldValue, newValue) -> headerNodeChanged(newValue));
            dataDetailPane.contentNodeProperty()
                          .addListener((observable, oldValue, newValue) -> contentNodeChanged(newValue));
            dataDetailPane.headerMonospaceProperty()
                          .addListener((observable, oldValue, newValue) -> monoHeaderChanged(newValue));
        }

        private void monoHeaderChanged(final Boolean newValue) {
            if (newValue) {
                headerHolder.getStyleClass().add(MONO_CLASS);
            } else {
                headerHolder.getStyleClass().remove(MONO_CLASS);
            }
        }

        private void headerNodeChanged(final Node newValue) {
            final Node newHeader = Objects.requireNonNullElseGet(newValue, this::createHeaderLabel);
            if (headerHolder.getChildren().isEmpty()) {
                headerHolder.getChildren().add(newHeader);
            } else {
                headerHolder.getChildren().set(0, newHeader);
            }

            if (newValue == null && !dataDetailPane.headerNodeProperty().isBound()) {
                dataDetailPane.setHeaderNode(headerHolder);
            }
        }

        private void contentNodeChanged(final Node newValue) {
            final boolean hasContent = node.getChildren().size() > 1;
            if (newValue == null && hasContent) {
                node.getChildren().remove(1);
            } else if (hasContent) {
                node.getChildren().set(1, newValue);
            } else {
                node.getChildren().add(newValue);
            }
        }

        private void createNode() {
            headerHolder = new StackPane();
            headerHolder.getStyleClass().add(HEADER_PANE_STYLE_CLASS);
            node = new VBox();
            node.getChildren().add(headerHolder);

            headerNodeChanged(dataDetailPane.getHeaderNode());
            contentNodeChanged(dataDetailPane.getContentNode());
            monoHeaderChanged(dataDetailPane.isHeaderMonospace());
        }

        private Node createHeaderLabel() {
            final Label label = new Label();
            label.textProperty().bind(dataDetailPane.headerTextProperty());
            label.getStyleClass().add(HEADER_PANE_LABEL_STYLE_CLASS);
            return label;
        }

        @Override
        public DataDetailPane getSkinnable() {
            return dataDetailPane;
        }

        @Override
        public Node getNode() {
            return node;
        }

        @Override
        public void dispose() {
            dataDetailPane = null;
            node = null;
        }
    }
}
