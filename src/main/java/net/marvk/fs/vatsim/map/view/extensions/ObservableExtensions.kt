package net.marvk.fs.vatsim.map.view.extensions

import javafx.beans.binding.Bindings
import javafx.beans.binding.IntegerBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.FloatProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.ReadOnlyFloatProperty
import javafx.beans.property.ReadOnlyFloatWrapper
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyLongProperty
import javafx.beans.property.ReadOnlyLongWrapper
import javafx.beans.property.ReadOnlyMapWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.ReadOnlySetWrapper
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableFloatValue
import javafx.beans.value.ObservableIntegerValue
import javafx.beans.value.ObservableListValue
import javafx.beans.value.ObservableLongValue
import javafx.beans.value.ObservableMapValue
import javafx.beans.value.ObservableSetValue
import javafx.beans.value.ObservableStringValue
import javafx.beans.value.ObservableValue
import javafx.beans.value.WritableBooleanValue
import javafx.beans.value.WritableDoubleValue
import javafx.beans.value.WritableFloatValue
import javafx.beans.value.WritableIntegerValue
import javafx.beans.value.WritableListValue
import javafx.beans.value.WritableLongValue
import javafx.beans.value.WritableMapValue
import javafx.beans.value.WritableObjectValue
import javafx.beans.value.WritableSetValue
import javafx.beans.value.WritableStringValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import kotlin.reflect.KProperty

fun <T> Iterable<T>.toUnmodifiableObservableList(): ObservableList<T> =
    FXCollections.observableList(toList()).let(FXCollections::unmodifiableObservableList)

fun <T> Iterable<T>.toUnmodifiableObservableSet(): ObservableSet<T> =
    FXCollections.observableSet(toSet()).let(FXCollections::unmodifiableObservableSet)

fun <K, V> Map<K, V>.toUnmodifiableObservableSet(): ObservableMap<K,V> =
    FXCollections.observableMap(this).let(FXCollections::unmodifiableObservableMap)

class UnmodifiableObservableListDelegate<E : Any?>(source: ObservableList<E>) {
    private val readOnlyList: ObservableList<E> = FXCollections.unmodifiableObservableList(source)
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = readOnlyList
}

class UnmodifiableObservableSetDelegate<E : Any?>(source: ObservableSet<E>) {
    private val readOnlySet: ObservableSet<E> = FXCollections.unmodifiableObservableSet(source)
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = readOnlySet
}

class UnmodifiableObservableMapDelegate<K : Any, V : Any>(source: ObservableMap<K, V>) {
    private val readOnlyMap: ObservableMap<K, V> = FXCollections.unmodifiableObservableMap(source)
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = readOnlyMap
}

fun <E : Any?> unmodifiable(observableList: ObservableList<E>) = UnmodifiableObservableListDelegate(observableList)
fun <E : Any?> unmodifiable(observableSet: ObservableSet<E>) = UnmodifiableObservableSetDelegate(observableSet)
fun <K : Any, V : Any> unmodifiable(observableMap: ObservableMap<K, V>) = UnmodifiableObservableMapDelegate(observableMap)

val <E : Any?> ObservableList<E>.readOnly: ObservableList<E> get() = FXCollections.unmodifiableObservableList(this)
val <E : Any?> ObservableList<E>.sizeBinding: IntegerBinding get() = Bindings.size(this)

val <E : Any?> ObservableSet<E>.readOnly: ObservableSet<E> get() = FXCollections.unmodifiableObservableSet(this)
val <E : Any?> ObservableSet<E>.sizeBinding: IntegerBinding get() = Bindings.size(this)

val <K : Any, V : Any> ObservableMap<K, V>.readOnly: ObservableMap<K, V> get() = FXCollections.unmodifiableObservableMap(this)
val <K : Any, V : Any> ObservableMap<K, V>.sizeBinding: IntegerBinding get() = Bindings.size(this)

operator fun <T : ObservableListValue<E>, E : Any> T.getValue(thisRef: Any?, property: KProperty<*>): ObservableList<E> = this.get()
operator fun <T : WritableListValue<E>, E : Any> T.setValue(thisRef: Any?, property: KProperty<*>, value: ObservableList<E>) = this.set(value)
val <E : Any?> ReadOnlyListWrapper<E>.readOnly: ObservableList<E> get() = readOnlyProperty
val <E : Any?> ReadOnlyListWrapper<E>.observableList: ObservableList<E> get() = this

operator fun <T : ObservableSetValue<E>, E : Any> T.getValue(thisRef: Any?, property: KProperty<*>): ObservableSet<E> = this.get()
operator fun <T : WritableSetValue<E>, E : Any> T.setValue(thisRef: Any?, property: KProperty<*>, value: ObservableSet<E>) = this.set(value)
val <E : Any?> ReadOnlySetWrapper<E>.readOnly: ObservableSet<E> get() = readOnlyProperty
val <E : Any?> ReadOnlySetWrapper<E>.observableSet: ObservableSet<E> get() = this

operator fun <T : ObservableMapValue<K, V>, K : Any, V : Any> T.getValue(thisRef: Any?, property: KProperty<*>): ObservableMap<K, V> = this.get()
operator fun <T : WritableMapValue<K, V>, K : Any, V : Any> T.setValue(thisRef: Any?, property: KProperty<*>, value: ObservableMap<K, V>) = this.set(value)
val <K : Any, V : Any> ReadOnlyMapWrapper<K, V>.readOnly: ObservableMap<K, V> get() = readOnlyProperty
val <K : Any, V : Any> ReadOnlyMapWrapper<K, V>.observableMap: ObservableMap<K, V> get() = this

operator fun <T : ObservableValue<E>, E : Any?> T.getValue(thisRef: Any?, property: KProperty<*>): E = this.value
operator fun <T : WritableObjectValue<E>, E : Any?> T.setValue(thisRef: Any?, property: KProperty<*>, value: E) = this.set(value)
val <E : Any?> ReadOnlyObjectWrapper<E>.readOnly: ReadOnlyObjectProperty<E> get() = readOnlyProperty
val <E : Any?> ReadOnlyObjectWrapper<E>.property: ObjectProperty<E> get() = this

operator fun <T : ObservableBooleanValue> T.getValue(thisRef: Any?, property: KProperty<*>): Boolean = this.get()
operator fun <T : WritableBooleanValue> T.setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) = this.set(value)
val ReadOnlyBooleanWrapper.readOnly: ReadOnlyBooleanProperty get() = readOnlyProperty
val ReadOnlyBooleanWrapper.property: BooleanProperty get() = this

operator fun <T : ObservableIntegerValue> T.getValue(thisRef: Any?, property: KProperty<*>): Int = this.get()
operator fun <T : WritableIntegerValue> T.setValue(thisRef: Any?, property: KProperty<*>, value: Int) = this.set(value)
val ReadOnlyIntegerWrapper.readOnly: ReadOnlyIntegerProperty get() = readOnlyProperty
val ReadOnlyIntegerWrapper.property: IntegerProperty get() = this

operator fun <T : ObservableLongValue> T.getValue(thisRef: Any?, property: KProperty<*>): Long = this.get()
operator fun <T : WritableLongValue> T.setValue(thisRef: Any?, property: KProperty<*>, value: Long) = this.set(value)
val ReadOnlyLongWrapper.readOnly: ReadOnlyLongProperty get() = readOnlyProperty
val ReadOnlyLongWrapper.property: LongProperty get() = this

operator fun <T : ObservableFloatValue> T.getValue(thisRef: Any?, property: KProperty<*>): Float = this.get()
operator fun <T : WritableFloatValue> T.setValue(thisRef: Any?, property: KProperty<*>, value: Float) = this.set(value)
val ReadOnlyFloatWrapper.readOnly: ReadOnlyFloatProperty get() = readOnlyProperty
val ReadOnlyFloatWrapper.property: FloatProperty get() = this

operator fun <T : ObservableDoubleValue> T.getValue(thisRef: Any?, property: KProperty<*>): Double = this.get()
operator fun <T : WritableDoubleValue> T.setValue(thisRef: Any?, property: KProperty<*>, value: Double) = this.set(value)
val ReadOnlyDoubleWrapper.readOnly: ReadOnlyDoubleProperty get() = readOnlyProperty
val ReadOnlyDoubleWrapper.property: DoubleProperty get() = this

operator fun <T : ObservableStringValue> T.getValue(thisRef: Any?, property: KProperty<*>): String = this.get()
operator fun <T : WritableStringValue> T.setValue(thisRef: Any?, property: KProperty<*>, value: String) = this.set(value)
val ReadOnlyStringWrapper.readOnly: ReadOnlyStringProperty get() = readOnlyProperty
val ReadOnlyStringWrapper.property: StringProperty get() = this
