package net.marvk.fs.vatsim.map.view.vatprism2.controls;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SectionHeader extends HBox {
    private final Label label = new Label();

    public SectionHeader(final String text) {
        label.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                if (newValue == null || newValue.isBlank()) {
                    getChildren().remove(label);
                } else if (!getChildren().contains(label)) {
                    getChildren().add(0, label);
                }
            }
        });
        setText(text);
        final Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.getStyleClass().add("no-padding");
        HBox.setHgrow(separator, Priority.ALWAYS);
        getChildren().add(separator);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(5);
    }

    public SectionHeader() {
        this("");
    }

    public StringProperty textProperty() {
        return label.textProperty();
    }

    public String getText() {
        return label.getText();
    }

    public void setText(final String value) {
        label.setText(value);
    }
}

