package net.marvk.fs.vatsim.map.view.vatprism2.controls

import javafx.scene.control.Button
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon

class IconButton() : Button() {
    private val icon = FontIcon()

    val iconColorProperty get() = icon.iconCodeProperty()
    var iconColor by iconColorProperty

    val iconSizeProperty get() = icon.iconSizeProperty()
    var iconSize: Int by iconSizeProperty

    var iconLiteral: String by icon::iconLiteral

    var iconCode: Ikon by icon::iconCode

    constructor(iconCode: Ikon) : this() {
        this.iconCode = iconCode
    }

    init {
        styleClass += "icon-button2"
        graphic = icon
    }
}
