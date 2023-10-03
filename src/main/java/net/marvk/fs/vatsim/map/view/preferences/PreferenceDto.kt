package net.marvk.fs.vatsim.map.view.preferences

import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import net.marvk.fs.vatsim.map.view.painter.Parameter

sealed interface PreferenceDto {
    val key: String
    val parameter: Parameter

    data class Int(override val key: String, override val parameter: Parameter, val property: IntegerProperty) : PreferenceDto
    data class Double(override val key: String, override val parameter: Parameter, val property: DoubleProperty) : PreferenceDto
    data class Color(override val key: String, override val parameter: Parameter, val property: ObjectProperty<javafx.scene.paint.Color>) : PreferenceDto
    data class Boolean(override val key: String, override val parameter: Parameter, val property: BooleanProperty) : PreferenceDto
}
