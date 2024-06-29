package net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox

import de.saxsys.mvvmfx.FluentViewLoader
import de.saxsys.mvvmfx.ViewTuple
import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox.dialog.DialogView
import net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox.dialog.DialogViewModel

class PatternBoxDialog : Alert(AlertType.NONE) {

    private val dialogContent: ViewTuple<DialogView, DialogViewModel> by lazy {
        FluentViewLoader
            .fxmlView(DialogView::class.java)
            .load()
    }

    val viewModel: DialogViewModel get() = dialogContent.viewModel

    val okButton = ButtonType("OK", ButtonBar.ButtonData.OK_DONE)
    val cancelButton = ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)

    init {
        dialogPane.apply {
            stylesheets += javaClass.getResource("/net/marvk/fs/vatsim/map/view/root.css")!!.toExternalForm()
            stylesheets += javaClass.getResource("/net/marvk/fs/vatsim/map/view/filter/filtereditor/patternbox/dialog/dialog.css")!!.toExternalForm()
        }
        dialogPane.content = dialogContent.view
        dialogPane.children.removeFirst()
        onCloseRequest = EventHandler { close() }
        buttonTypes += okButton
        buttonTypes += cancelButton
        dialogPane
            .lookupButton(okButton)
            .disableProperty()
            .bind(
                Bindings.createBooleanBinding(
                    {
                        with(dialogContent.viewModel) {
                            !patternValid || pattern.isBlank()
                        }
                    },
                    dialogContent.viewModel.patternValidProperty,
                    dialogContent.viewModel.patternProperty
                )
            )
    }
}
