package net.marvk.fs.vatsim.map.view.vatprism2.controls

import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import net.marvk.fs.vatsim.map.data.Filter
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue

class AndOrToggle : Control() {
    val valueProperty: SimpleObjectProperty<Filter.Operator> = SimpleObjectProperty(Filter.Operator.OR)
    var value by valueProperty

    init {
        skin = createDefaultSkin()
    }

    override fun createDefaultSkin(): Skin<*> = DefaultSkin(this)

    private class DefaultSkin(control: AndOrToggle) : SkinBase<AndOrToggle>(control) {
        private val theToggleGroup = ToggleGroup().apply {

        }

        private val orRadio = RadioButton().apply {
            isSelected = true
            toggleGroup = theToggleGroup
        }
        private val andRadio = RadioButton().apply {
            toggleGroup = theToggleGroup
        }

        private val orLabel = Label("or")

        private val andLabel = Label("and")

        private val container = VBox(
            orRadio,
            orLabel,
            Region().apply { prefHeight = 10.0 },
            andRadio,
            andLabel,
        ).apply {
            alignment = Pos.CENTER
        }

        init {
            theToggleGroup.selectedToggleProperty().addListener { _, _, _ ->
                control.value = (if (orRadio.isSelected) Filter.Operator.OR else Filter.Operator.AND)
            }
            control.valueProperty.addListener { _, _, newValue ->
                if (newValue == Filter.Operator.OR) orRadio.isSelected = true else andRadio.isSelected = true
            }
            orLabel.onMousePressed = EventHandler { orRadio.requestFocus() }
            orLabel.onMouseClicked = EventHandler { orRadio.isSelected = true }
            andLabel.onMousePressed = EventHandler { andRadio.requestFocus() }
            andLabel.onMouseClicked = EventHandler { andRadio.isSelected = true }
            children.add(container)
        }
    }
}
