package net.marvk.fs.vatsim.map.view.filter.filtereditor.patternbox.dialog

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.binding.Bindings
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.readOnly
import net.marvk.fs.vatsim.map.view.extensions.setValue

class DialogViewModel : ViewModel {
    val nameProperty = SimpleStringProperty("")
    var name by nameProperty

    val patternProperty = SimpleStringProperty("")
    var pattern by patternProperty

    val regexProperty = SimpleBooleanProperty(false)
    var regex by regexProperty

    private val patternValidPropertyWritable = ReadOnlyBooleanWrapper(false)
    val patternValidProperty = patternValidPropertyWritable.readOnly
    val patternValid by patternValidProperty

    private val regexResultPropertyWritable: ReadOnlyObjectWrapper<Result<Regex>> = ReadOnlyObjectWrapper(Result.success("".toRegex())).apply {
        bind(Bindings.createObjectBinding({ runCatching { pattern.toRegex() } }, patternProperty))
    }
    val regexResultProperty = regexResultPropertyWritable.readOnly
    val regexResult: Result<Regex> by regexResultProperty

    init {
        patternValidPropertyWritable.bind(Bindings.createBooleanBinding({
            regexResult.isSuccess || !regex
        }, regexResultProperty, regexProperty))
    }
}
