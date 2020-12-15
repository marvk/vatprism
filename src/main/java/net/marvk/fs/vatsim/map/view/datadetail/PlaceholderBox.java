package net.marvk.fs.vatsim.map.view.datadetail;

import javafx.beans.property.*;
import javafx.scene.Node;
import javafx.scene.control.*;

public class PlaceholderBox extends Control {
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final StringProperty placeholderText = new SimpleStringProperty();
    private final BooleanProperty contentVisible = new SimpleBooleanProperty();

    public PlaceholderBox() {
        setSkin(createDefaultSkin());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlaceholderBoxSkin(this);
    }

    public Node getContent() {
        return content.get();
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public void setContent(final Node content) {
        this.content.set(content);
    }

    public String getPlaceholderText() {
        return placeholderText.get();
    }

    public StringProperty placeholderTextProperty() {
        return placeholderText;
    }

    public void setPlaceholderText(final String placeholderText) {
        this.placeholderText.set(placeholderText);
    }

    public boolean isContentVisible() {
        return contentVisible.get();
    }

    public BooleanProperty contentVisibleProperty() {
        return contentVisible;
    }

    public void setContentVisible(final boolean contentVisible) {
        this.contentVisible.set(contentVisible);
    }

    private static class PlaceholderBoxSkin implements Skin<Skinnable> {
        private ScrollPane contentHolder;

        private PlaceholderBox placeholderBox;
        private Label label;

        public PlaceholderBoxSkin(final PlaceholderBox placeholderBox) {
            this.placeholderBox = placeholderBox;
            this.placeholderBox.content.addListener((observable, oldValue, newValue) -> recalculateChildren());
            this.placeholderBox.contentVisible.addListener((observable, oldValue, newValue) -> recalculateChildren());
            this.placeholderBox.placeholderText.addListener((observable, oldValue, newValue) -> recalculateChildren());

            this.contentHolder = new ScrollPane();

            this.label = new Label();
            this.label.textProperty().bind(placeholderBox.placeholderText);

            this.contentHolder.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            this.contentHolder.minHeightProperty().bind(placeholderBox.minHeightProperty());
            this.contentHolder.minWidthProperty().bind(placeholderBox.minWidthProperty());

            this.contentHolder.prefHeightProperty().bind(placeholderBox.prefHeightProperty());
            this.contentHolder.prefWidthProperty().bind(placeholderBox.prefWidthProperty());

            this.contentHolder.maxHeightProperty().bind(placeholderBox.maxHeightProperty());
            this.contentHolder.maxWidthProperty().bind(placeholderBox.maxWidthProperty());

            this.contentHolder.setStyle("""
                    -fx-background: transparent;
                    -fx-background-color: transparent;
                    """);
        }

        private void recalculateChildren() {
            final Node content = placeholderBox.content.get();
            final boolean contentVisible = placeholderBox.contentVisible.get();
            if (!contentVisible || content == null) {
                contentHolder.setContent(label);
            } else {
                contentHolder.setContent(placeholderBox.content.get());
            }
        }

        @Override
        public Skinnable getSkinnable() {
            return placeholderBox;
        }

        @Override
        public Node getNode() {
            return contentHolder;
        }

        @Override
        public void dispose() {
            this.placeholderBox = null;
            this.contentHolder = null;
            this.label = null;
        }
    }
}
