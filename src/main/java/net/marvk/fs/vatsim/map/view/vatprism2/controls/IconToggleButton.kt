package net.marvk.fs.vatsim.map.view.vatprism2.controls

import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.control.ToggleButton
import net.marvk.fs.vatsim.map.data.ImmutableObjectProperty
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue
import org.kordamp.ikonli.Ikon
import org.kordamp.ikonli.javafx.FontIcon


class IconToggleButton @JvmOverloads constructor(
    styleClass: String = "icon-button2",
) : ToggleButton() {
    val iconProperty: ReadOnlyObjectProperty<FontIcon> = ImmutableObjectProperty(FontIcon());
    val icon: FontIcon by iconProperty

    val iconColorProperty get() = icon.iconColorProperty()
    var iconColor by iconColorProperty

    val iconSizeProperty get() = icon.iconSizeProperty()
    var iconSize: Int by iconSizeProperty

    val iconCodeProperty: ObjectProperty<Ikon?> get() = icon.iconCodeProperty()
    var iconCode: Ikon? by iconCodeProperty

    var iconLiteral: String? by icon::iconLiteral

    init {
        graphic = icon
        this.styleClass += styleClass
    }
}
