package net.marvk.fs.vatsim.map.view.vatprism2.controls

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.binding.Bindings
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignC
import org.kordamp.ikonli.materialdesign2.MaterialDesignM

class VatprismCheckBoxSkin(control: CheckBox) : SkinBase<CheckBox>(control) {
    private var label: Label? = Label()
    private var icon: FontIcon? =
        FontIcon(MaterialDesignC.CHECK_BOLD)
            .apply {
                iconSize = 12
            }

    private var container: StackPane? =
        StackPane(icon).apply {
            styleClass += "box"
        }

    private var buttonBehavior: ButtonBehavior<CheckBox>? = ButtonBehavior(control)

    private var actualContainer: HBox? = HBox(container, label).apply {
        spacing = 5.0
        alignment = Pos.CENTER_LEFT
    }

    init {
        label!!.textProperty().bind(control.textProperty())
        control.styleClass += "vatprism-check-box"
        children += actualContainer
        icon!!.iconCodeProperty().bind(Bindings.createObjectBinding({
            if (control.isIndeterminate) {
                MaterialDesignM.MINUS_THICK
            } else if (control.isSelected) {
                MaterialDesignC.CHECK_BOLD
            } else {
                null
            }
        }))
    }

    override fun dispose() {
        super.dispose()

        actualContainer = null
        buttonBehavior = null
        container = null
        icon = null
    }
}
