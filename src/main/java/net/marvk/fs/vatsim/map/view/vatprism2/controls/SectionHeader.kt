package net.marvk.fs.vatsim.map.view.vatprism2.controls

import javafx.beans.property.StringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority

class SectionHeader constructor(text: String? = "") : HBox() {
    private val label = Label()

    var text: String? by label::text

    init {
        label.textProperty().addListener { _, _, newValue: String? ->
            if (newValue.isNullOrBlank()) {
                children.remove(label)
            } else if (!children.contains(label)) {
                children.add(0, label)
            }
        }
        this.text = text
        val separator = Separator(Orientation.HORIZONTAL).apply {
            styleClass.add("no-padding")
            setHgrow(this, Priority.ALWAYS)
        }
        children.add(separator)
        alignment = Pos.CENTER_LEFT
        spacing = 5.0
    }

    fun textProperty(): StringProperty = label.textProperty()
}
