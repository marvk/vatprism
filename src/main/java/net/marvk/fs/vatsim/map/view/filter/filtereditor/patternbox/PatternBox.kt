package net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.SelectionMode
import javafx.scene.control.Tooltip
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Callback
import javafx.util.Duration
import net.marvk.fs.vatsim.map.extensions.getOrNull
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue
import net.marvk.fs.vatsim.map.view.filter.FilterStringListViewModel
import net.marvk.fs.vatsim.map.view.vatprism2.controls.ButtonBarBox
import net.marvk.fs.vatsim.map.view.vatprism2.controls.IconButton
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignF
import org.kordamp.ikonli.materialdesign2.MaterialDesignM
import org.kordamp.ikonli.materialdesign2.MaterialDesignP
import org.kordamp.ikonli.materialdesign2.MaterialDesignR

class PatternBox : ButtonBarBox<ListView<FilterStringListViewModel>>() {
    private val listView: ListView<FilterStringListViewModel> = ListView()

    private val dialog = PatternBoxDialog()

    private val addButton = IconButton(MaterialDesignP.PLUS).apply { onAction = EventHandler { add() } }
    private val removeButton = IconButton(MaterialDesignM.MINUS).apply { onAction = EventHandler { removeSelected() } }
    private val editButton = IconButton(MaterialDesignP.PENCIL).apply { onAction = EventHandler { editSelected() } }

    val selectedItemProperty: ReadOnlyObjectProperty<FilterStringListViewModel?> get() = listView.selectionModel.selectedItemProperty()
    val selectedItem: FilterStringListViewModel? by selectedItemProperty

    val typeProperty = SimpleStringProperty()
    var type by typeProperty

    val items: ObservableList<FilterStringListViewModel> = listView.items

    init {
        with(listView.selectionModel) {
            selectionMode = SelectionMode.SINGLE
            removeButton.disableProperty().bind(selectedItemProperty().isNull)
            editButton.disableProperty().bind(selectedItemProperty().isNull)
        }
        buttonBarChildren.addAll(addButton, removeButton, editButton)
        listView.cellFactory = Callback { _ -> SelectionBoxListCell() }
        content = listView

        skin.node.sceneProperty().addListener { _, _, scene -> dialog.initOwner(scene.window) }
    }


    private fun add() {
        dialog.viewModel.apply {
            name = ""
            pattern = ""
        }

        dialog
            .apply { title = "Add $type" }
            .showAndWait()
            .getOrNull()
            ?.takeIf { it == dialog.okButton }
            ?.also {
                dialog
                    .viewModel
                    .run { FilterStringListViewModel(name, pattern, regex) }
                    .also(items::add)
            }
    }

    private fun removeSelected() {
        selectedItem?.also(items::remove)
    }

    private fun editSelected() {
        val selectedItem = selectedItem ?: return

        dialog.viewModel.apply {
            name = selectedItem.name
            pattern = selectedItem.content
            regex = selectedItem.isRegex
        }

        dialog
            .apply { title = "Edit $type" }
            .showAndWait()
            .getOrNull()
            ?.takeIf { it == dialog.okButton }
            ?.also {
                selectedItem.apply {
                    name = dialog.viewModel.name
                    content = dialog.viewModel.pattern
                    isRegex = dialog.viewModel.regex
                }
            }
    }

    private inner class SelectionBoxListCell : ListCell<FilterStringListViewModel>() {
        private val nameLabel = Label().apply {
            styleClass += "faint"
        }
        private val patternLabel = Label().apply {
            styleClass += "mono"
            alignment = Pos.CENTER_RIGHT
        }
        private val icon = FontIcon(MaterialDesignR.REGEX).apply {
            padding = Insets(0.0, 10.0, 0.0, 0.0)
        }
        private val hBox = HBox(
            icon,
            patternLabel,
            Region().apply { HBox.setHgrow(this, Priority.SOMETIMES) },
            nameLabel,
        ).apply {
            alignment = Pos.CENTER_LEFT
            prefHeight = 20.0
            spacing = 5.0
            padding = Insets(5.0)
        }

        init {
            Tooltip().apply {
                textProperty().bind(itemProperty().map { if (it.isRegex) "Regex" else "Wildcard" })
                hideDelay = Duration.ZERO
                showDelay = Duration.ZERO
                Tooltip.install(icon, this);
            }
            graphicProperty().bind(itemProperty().map { hBox })
            styleClass += "vatprism-cell"
            onMouseClicked = EventHandler {
                if (it.clickCount >= 2) editSelected()
            }
        }

        override fun updateItem(item: FilterStringListViewModel?, empty: Boolean) {
            super.updateItem(item, empty)

            if (item == null) {
                nameLabel.textProperty().unbind()
                patternLabel.textProperty().unbind()
                icon.iconCodeProperty().unbind()
                nameLabel.text = null
                patternLabel.text = null
            } else {
                nameLabel.textProperty().bind(item.nameProperty())
                patternLabel.textProperty().bind(item.contentProperty())
                icon.iconCodeProperty().bind(item.regexProperty().map {
                    if (it) MaterialDesignR.REGEX else MaterialDesignF.FORMAT_TEXT_VARIANT
                })
            }
        }
    }
}
