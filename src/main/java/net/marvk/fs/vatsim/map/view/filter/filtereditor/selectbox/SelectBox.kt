package net.marvk.fs.vatsim.map.view.filter.filtereditor.selectbox

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Callback
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue
import net.marvk.fs.vatsim.map.view.vatprism2.controls.ButtonBarBox

class SelectBox<Item> : ButtonBarBox<ListView<SelectBox<Item>.ListViewModel>>() {
    val cellValueFactoryProperty = SimpleObjectProperty<(Item) -> String> { it.toString() }
    var cellValueFactory: (Item) -> String by cellValueFactoryProperty

    val availableItems:ObservableList<Item> = FXCollections.observableArrayList()

    val selectedItems: ObservableList<Item> = FXCollections.observableArrayList()

    val listView = ListView<ListViewModel>()
    private val viewModels = FXCollections.observableArrayList<ListViewModel> { arrayOf(it.selectedProperty, it.itemProperty) }

    init {
        hideButtonBar = true
        content = listView
        listView.items = viewModels
        availableItems.addListener(ListChangeListener {
            viewModels.setAll(availableItems.map(::ListViewModel))
        })
        viewModels.addListener(ListChangeListener {
            selectedItems.setAll(viewModels.filter { it.selected }.map { it.item })
        })
        listView.cellFactory = Callback { ListCell() }
    }

    inner class ListCell : javafx.scene.control.ListCell<ListViewModel>() {
        private val label = Label()
        private val checkBox = CheckBox().apply {
            styleClass += "check-box-standalone"
        }

        private val container = HBox(
            label,
            Region().apply { HBox.setHgrow(this, Priority.SOMETIMES) },
            checkBox,
        ).apply {
            styleClass += "vatprism-cell"
            prefHeight = 20.0
            alignment = Pos.CENTER_LEFT
            padding = Insets(0.0, 3.0, 0.0, 5.0)
        }

        init {
            graphicProperty().bind(itemProperty().map { container })
            itemProperty().addListener { _, oldValue: SelectBox<Item>.ListViewModel?, newValue: SelectBox<Item>.ListViewModel? ->
                if (oldValue != null) {
                    checkBox.selectedProperty().unbindBidirectional(oldValue.selectedProperty)
                }
                if (newValue == null) {
                    label.text = ""
                } else {
                    label.text = newValue.item.let(cellValueFactory)
                    checkBox.selectedProperty().bindBidirectional(newValue.selectedProperty)
                }
            }
        }
    }

    inner class ListViewModel(item: Item) {
        val itemProperty: SimpleObjectProperty<Item> = SimpleObjectProperty(item)
        var item by itemProperty

        val selectedProperty = SimpleBooleanProperty()
        var selected by selectedProperty
    }
}
