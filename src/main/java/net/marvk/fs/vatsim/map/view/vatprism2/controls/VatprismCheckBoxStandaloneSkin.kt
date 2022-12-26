package net.marvk.fs.vatsim.map.view.vatprism2.controls

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.beans.binding.Bindings
import javafx.scene.control.CheckBox
import javafx.scene.control.SkinBase
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignC
import org.kordamp.ikonli.materialdesign2.MaterialDesignM

class VatprismCheckBoxStandaloneSkin(control: CheckBox) : SkinBase<CheckBox>(control) {

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

    init {
        control.styleClass += "vatprism-check-box"
        children += container
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

        buttonBehavior = null
        container = null
        icon = null
    }
}
