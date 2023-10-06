package net.marvk.fs.vatsim.map.view.preferences

import com.google.inject.Inject
import com.google.inject.Singleton
import javafx.scene.paint.Color
import net.marvk.fs.vatsim.map.data.Preferences
import net.marvk.fs.vatsim.map.view.Notifications
import net.marvk.fs.vatsim.map.view.SettingsScope
import net.marvk.fs.vatsim.map.view.painter.MetaPainter
import net.marvk.fs.vatsim.map.view.painter.Painter
import net.marvk.fs.vatsim.map.view.painter.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible

@Singleton
class PainterPreferencesLoader @Inject constructor(
    private val settingsScope: SettingsScope,
    private val preferences: Preferences,
) {

    fun loadSettings() =
        settingsScope
            .painters
            .map { generatePreferences(it.painter, it.legacyName, it.name) }

    private fun generatePreferences(painter: Painter<*>, prefix: String, name: String): PainterPreferencesDto {
        val fields = getFields(painter)

        val subPainterPreferences =
            fields
                .filter { it.returnType.isSubtypeOf(Painter::class.starProjectedType) }
                .filter { it.annotations.filterIsInstance<MetaPainter>().any() }
                .map { painterField ->
                    painterField.isAccessible = true

                    val subPainter = painterField.getter.call(painter) as Painter<*>
                    val metaPainterAnnotation = painterField.annotations.filterIsInstance<MetaPainter>().single()

                    generatePreferences(subPainter, "$prefix.${metaPainterAnnotation.legacyName.takeIf(String::isNotBlank) ?: metaPainterAnnotation.name}", metaPainterAnnotation.name)
                }

        return fields
            .filterIsInstance<KMutableProperty<*>>()
            .filter { it.getParameterAnnotationOrNull() != null }
            .mapNotNull { generatePreferencesList(it, painter, prefix) }
            .let { preferences ->
                val (enabled, remaining) = preferences.partition { it.key.endsWith(".enabled") }

                val groupedPreferences = remaining.groupBy { it.parameter.group }.toMutableMap()

                val ungroupedPreferences = groupedPreferences.remove("") ?: listOf()

                PainterPreferencesDto(name, enabled.filterIsInstance<PreferenceDto.Boolean>().singleOrNull(), ungroupedPreferences, groupedPreferences, subPainterPreferences)
            }
    }

    private fun generatePreferencesList(field: KMutableProperty<*>, painter: Painter<*>, prefix: String): PreferenceDto? {
        field.isAccessible = true

        val parameter = field.getParameterAnnotation()

        val name = parameter.legacyName.takeIf(String::isNotBlank) ?: parameter.name
        val enabled = !parameter.disabled

        val key = calculateKey(prefix, name)

        if (field.returnType.isSubtypeOf(Color::class.createType())) {
            val property = preferences.colorProperty(key, field.getter.call(painter) as Color)
            property.addListener { _, _, newValue -> field.setAndRepaint(painter, newValue) }
            field.setter.call(painter, property.get())
            if (enabled) {
                return PreferenceDto.Color(key, parameter, property)
            }
        } else if (field.returnType.isSubtypeOf(Int::class.createType())) {
            val property = preferences.integerProperty(key, field.getter.call(painter) as Int)
            property.addListener { _, _, newValue -> field.setAndRepaint(painter, newValue) }
            field.setter.call(painter, property.get())
            if (enabled) {
                return PreferenceDto.Int(key, parameter, property)
            }
        } else if (field.returnType.isSubtypeOf(Double::class.createType())) {
            val property = preferences.doubleProperty(key, field.getter.call(painter) as Double)
            property.addListener { _, _, newValue -> field.setAndRepaint(painter, newValue) }
            field.setter.call(painter, property.get())
            if (enabled) {
                return PreferenceDto.Double(key, parameter, property)
            }
        } else if (field.returnType.isSubtypeOf(Boolean::class.createType())) {
            val property = preferences.booleanProperty(key, field.getter.call(painter) as Boolean)
            property.addListener { _, _, newValue -> field.setAndRepaint(painter, newValue) }
            field.setter.call(painter, property.get())
            if (enabled) {
                return PreferenceDto.Boolean(key, parameter, property)
            }
        }

        return null
    }
}

private fun KMutableProperty<*>.setAndRepaint(painter: Painter<*>, newValue: Any?) {
    setter.call(painter, newValue)
    Notifications.REPAINT.publish()
}

private fun getFields(painter: Painter<*>) =
    generateSequence(painter::class, KClass<*>::superclassOrNull)
        .flatMap { it.declaredMemberProperties }
        .toList()

private fun KClass<*>.superclassOrNull() = superclasses.filterNot { it.java.isInterface }.singleOrNull()
private fun KProperty<*>.getParameterAnnotationOrNull() = annotations.filterIsInstance<Parameter>().singleOrNull()
private fun KProperty<*>.getParameterAnnotation() = getParameterAnnotationOrNull()!!

private fun calculateKey(vararg keys: String) = keys
    .map { it.lowercase() }
    .map { it.replace("\\s".toRegex(), "_") }
    .map { it.replace("[^A-Za-z0-9._]".toRegex(), "") }
    .joinToString(".")
