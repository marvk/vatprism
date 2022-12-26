package net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox.dialog

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.scene.control.RadioButton
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.control.Tooltip
import javafx.util.Duration
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignA

class DialogView : FxmlView<DialogViewModel> {

    @FXML
    private lateinit var nameTextField: TextField

    @FXML
    private lateinit var patternTextField: CustomTextField

    @FXML
    private lateinit var regexRadio: RadioButton

    @FXML
    private lateinit var wildcardRadio: RadioButton

    @FXML
    private lateinit var matchingTypeToggleGroup: ToggleGroup

    @InjectViewModel
    private lateinit var viewModel: DialogViewModel


    private val errorIcon = FontIcon(MaterialDesignA.ALERT_CIRCLE).apply { styleClass += "error-icon" }
    private val errorTooltip = Tooltip()

    fun initialize() {
        regexRadio.selectedProperty().bindBidirectional(viewModel.regexProperty)
        patternTextField.textProperty().bindBidirectional(viewModel.patternProperty)
        nameTextField.textProperty().bindBidirectional(viewModel.nameProperty)
        patternTextField.rightProperty().bind(viewModel.patternValidProperty.map { if (it) null else errorIcon })
        errorTooltip.apply {
            textProperty().bind(viewModel.regexResultProperty.map { it.exceptionOrNull()?.message })
            hideDelay = Duration.ZERO
            showDelay = Duration.ZERO
            Tooltip.install(errorIcon, this)
        }
    }
}
