package net.marvk.fs.vatsim.map.view.preferences

import com.dlsc.formsfx.model.structure.Element
import com.dlsc.formsfx.model.structure.NodeElement
import com.dlsc.preferencesfx.PreferencesFx
import com.dlsc.preferencesfx.formsfx.view.renderer.PreferencesFxFormRenderer
import com.dlsc.preferencesfx.formsfx.view.renderer.PreferencesFxGroupRenderer
import com.dlsc.preferencesfx.model.Category
import com.dlsc.preferencesfx.model.Group
import com.dlsc.preferencesfx.model.PreferencesFxModel
import com.dlsc.preferencesfx.model.Setting
import com.dlsc.preferencesfx.util.VisibilityProperty
import com.dlsc.preferencesfx.view.BreadCrumbView
import com.dlsc.preferencesfx.view.CategoryController
import com.dlsc.preferencesfx.view.CategoryView
import com.dlsc.preferencesfx.view.FilterableTreeItem
import com.dlsc.preferencesfx.view.NavigationPresenter
import com.dlsc.preferencesfx.view.NavigationView
import com.dlsc.preferencesfx.view.PreferencesFxDialog
import com.dlsc.preferencesfx.view.PreferencesFxView
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.TreeView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import net.marvk.fs.vatsim.map.data.ImmutableBooleanProperty
import net.marvk.fs.vatsim.map.view.vatprism2.controls.SectionHeader
import org.controlsfx.control.BreadCrumbBar
import org.controlsfx.control.MasterDetailPane
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign2.MaterialDesignC
import org.kordamp.ikonli.materialdesign2.MaterialDesignM
import org.scenicview.ScenicView
import java.util.UUID
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

@DslMarker
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class PreferencesFxMarker

fun preferencesFx(saveClass: Class<*>, block: PreferencesFxContext.() -> Unit): PreferencesFx {
    val context = PreferencesFxContext().apply(block)
    return PreferencesFx.of(saveClass, *context.categories.toTypedArray())
        .apply(PreferencesFx::modifyPreferencesFxStyle)
        .also { context.preferencesFx = it }
}

@PreferencesFxMarker
class PreferencesFxContext() {
    lateinit var preferencesFx: PreferencesFx

    val categories = mutableListOf<Category>()

    fun category(name: String, superCategoryDescription: String? = null, block: CategoryContext.() -> Unit) {
        categories +=
            CategoryContext(name)
                .apply(block)
                .apply { applySuperCategoryDescription(superCategoryDescription) }
                .build()
    }
}

@PreferencesFxMarker
class CategoryContext(val name: String) {
    var icon: Node? = null
    var visibilityProperty: VisibilityProperty? = null
    val groups = mutableListOf<Group>()
    val subCategories = mutableListOf<Category>()

    fun category(name: String, superCategoryDescription: String? = null, block: CategoryContext.() -> Unit) {
        subCategories +=
            CategoryContext(name)
                .apply(block)
                .apply { applySuperCategoryDescription(superCategoryDescription) }
                .build()
    }

    fun group(name: String? = null, block: GroupContext.() -> Unit) {
        groups += GroupContext(name).apply(block).let {
            when (it.visibilityProperty) {
                null -> when (groups.size) {
                    0 -> Group.of(*it.settings.toTypedArray())
                    else -> Group.of(it.name, *it.settings.toTypedArray())
                }

                else -> when (groups.size) {
                    0 -> Group.of(it.visibilityProperty, *it.settings.toTypedArray())
                    else -> Group.of(it.name, it.visibilityProperty, *it.settings.toTypedArray())
                }
            }
        }
    }
}

@PreferencesFxMarker
class GroupContext(val name: String?) {
    var visibilityProperty: VisibilityProperty? = null
    var settings = mutableListOf<Setting<*, *>>()

    private class VatprismNodeElement(node: Node) : NodeElement<Node>(node)

    private class VatprismSetting(
        private val searchText: String,
        private val node: Node,
        value: Property<*>?,
    ) : Setting<Element<*>, Property<*>>(
        UUID.randomUUID().toString(),
        VatprismNodeElement(node),
        value
    ) {
        override fun getDescription() = searchText

        override fun mark() {
            if (!node.styleClass.contains("simple-control-marked")) {
                node.styleClass += "simple-control-marked"
            }
        }

        override fun unmark() {
            node.styleClass -= "simple-control-marked"
        }
    }

    fun checkbox(booleanProperty: BooleanProperty, block: SettingContext.() -> Unit = {}) {
        setting(CheckBox(SettingContext().apply(block).textAfter).also { it.selectedProperty().bindBidirectional(booleanProperty) }, booleanProperty) {
            block()
            textAfter?.also(hiddenSearchableText::add)
            textAfter = null
        }
    }

    fun doubleSpinner(min: Double, max: Double, amountToStepBy: Double, doubleProperty: DoubleProperty, block: SettingContext.() -> Unit = {}) {
        setting(Spinner<Double>(min, max, min, amountToStepBy).also { it.valueFactory.valueProperty().bindBidirectional(doubleProperty.asObject()) }, doubleProperty) { block() }
    }

    fun intSpinner(min: Int, max: Int, amountToStepBy: Int, integerProperty: IntegerProperty, block: SettingContext.() -> Unit = {}) {
        setting(Spinner<Int>(min, max, min, amountToStepBy).also { it.valueFactory.valueProperty().bindBidirectional(integerProperty.asObject()) }, integerProperty) { block() }
    }

    fun button(label: String, action: () -> Unit, block: SettingContext.() -> Unit = {}) {
        setting(Button(label).apply { onAction = EventHandler { action() } }) { block() }
    }

    fun <T> combobox(options: ObservableList<T>, objectProperty: ObjectProperty<T>, block: SettingContext.() -> Unit = {}) {
        require(options.size >= 3)
        setting(ComboBox(options).also { it.valueProperty().bindBidirectional(objectProperty) }, objectProperty) { block() }
    }

    fun label(label: String, block: SettingContext.() -> Unit = {}) {
        setting(Label(label)) { block() }
    }

    fun setting(node: Node, vararg observableValues: Property<*>, block: SettingContext.() -> Unit = {}) {
        SettingContext().apply(block).let { context ->
            settings +=
                listOfNotNull(context.textBefore?.let(::Label), node, context.textAfter?.let(::Label))
                    .toTypedArray()
                    .let { if (it.size == 1) it.first() else HBox(*it).apply { spacing = 5.0; alignment = Pos.CENTER_LEFT } }
                    .let { controlBox ->
                        context
                            .textExplanation
                            ?.let(::Label)
                            ?.apply { styleClass += "faint" }
                            ?.let { HBox(Region().apply { prefWidth = 17.0 }, it).apply { spacing = 2.0; alignment = Pos.CENTER_LEFT } }
                            ?.let { VBox(controlBox, it) }
                            ?: controlBox
                    }
                    .also { node -> context.disableProperty?.also { node.disableProperty().bind(it) } }
                    .let {
                        VatprismSetting(
                            (context.hiddenSearchableText + listOfNotNull(context.textAfter, context.textBefore, context.textExplanation)).joinToString(separator = " "),
                            it,
                            observableValues.firstOrNull()
                        )
                    }

            settings += observableValues.drop(1).map { it.toInvisibleSetting() }
        }
    }

    private fun Property<*>.toInvisibleSetting() = VatprismSetting("", Region(), this).invisible()
}

@PreferencesFxMarker
class SettingContext {
    var textBefore: String? = null
    var textAfter: String? = null
    var textExplanation: String? = null
    var disableProperty: ObservableBooleanValue? = null
    val hiddenSearchableText = mutableListOf<String>()
}

fun PreferencesFx.modifyPreferencesFxStyle() {
    view.parent.stylesheets.setAll(
        PreferencesView::class.java.getResource("/net/marvk/fs/vatsim/map/view/root.css")!!.toExternalForm(),
        PreferencesView::class.java.getResource("/net/marvk/fs/vatsim/map/view/scrollbar.css")!!.toExternalForm(),
        PreferencesView::class.java.getResource("/net/marvk/fs/vatsim/map/view/preferences/preferences.css")!!.toExternalForm(),
    )

    buttonBar.safeButtons.also {
        it[0].apply {
            isDefaultButton = true
            isCancelButton = false
            text = "OK"
        }
        it[1].apply {
            text = "Cancel"
        }
    }

    preferencesFxDialog.preferencesFxView.navigationView.apply {
        searchField.apply {
            left = FontIcon(MaterialDesignM.MAGNIFY).apply { styleClass += "custom-text-field-icon" }
        }
        treeView.apply {
            Platform.runLater {
                selectionModel.select(navigationPresenter.categoryTreeItemMap[preferencesFxModel.displayedCategory])
            }
        }
    }

    preferencesFxDialog
        .preferencesFxView
        .categoryController
        .views
        .values
        .map(CategoryView::preferencesFormRenderer)
        .forEach { grid ->
            grid.padding = Insets(2.0, 16.0, 16.0, 17.0)

            grid.columnConstraints.setAll(
                ColumnConstraints().apply { hgrow = Priority.SOMETIMES },
                ColumnConstraints().apply { hgrow = Priority.ALWAYS },
            )

            val (titles, others) =
                grid
                    .children
                    .partition { it.styleClass.contains("group-title") }

            titles
                .filterIsInstance<Label>()
                .map { groupTitle ->
                    SectionHeader(groupTitle.text)
                        .also {
                            grid.replace(groupTitle, it)
                            GridPane.setColumnSpan(it, 2)
                        }
                }
                .forEach {
                    GridPane.setMargin(it, Insets(11.0, 0.0, 0.0, 0.0))
                }

            others.forEachIndexed { index, node ->
                GridPane.setMargin(node, Insets(if (index == 0) 0.0 else 11.0, 0.0, 0.0, 10.0))
            }
        }

    preferencesFxDialog.preferencesFxView.breadCrumbView.also { view ->
        val newBreadCrumbs = BreadCrumb2(preferencesFxModel.displayedCategoryProperty()).apply { HBox.setHgrow(this, Priority.ALWAYS) }
        view.children.replace(view.breadCrumbBar, newBreadCrumbs)
    }

    ScenicView.show(view.scene)
}

private class BreadCrumb2(categoryProperty: ObservableValue<Category>) : HBox() {
    init {
        alignment = Pos.CENTER_LEFT
        padding = Insets(0.0, 0.0, 0.0, 17.0)
        styleClass += "structure"
        spacing = 10.0

        categoryProperty.addListener { _, _, category ->
            setChildren(category)
        }

        setChildren(categoryProperty.value)
    }

    private fun setChildren(category: Category?) {
        category ?: return

        category
            .breadcrumb
            .split("#")
            .flatMapIndexed { index: Int, s: String ->
                buildList {
                    if (index != 0) {
                        add(FontIcon.of(MaterialDesignC.CHEVRON_RIGHT, 14))
                    }

                    add(Label(s).apply { styleClass += "bold" })
                }
            }
            .also(children::setAll)
    }
}

private fun MutableList<Node>.replace(from: Node, to: Node) {
    val index = indexOf(from)
    require(index >= 0)
    removeAt(index)
    add(index, to)
}

private val INVISIBLE = VisibilityProperty.of<Nothing>(ImmutableBooleanProperty(false))
private fun <E : Element<*>, P : Property<*>> Setting<E, P>.invisible() = also { applyVisibility(INVISIBLE) }

private fun GridPane.replace(from: Node, to: Node) {
    require(children.contains(from))
    GridPane.setColumnIndex(to, GridPane.getColumnIndex(from))
    GridPane.setRowIndex(to, GridPane.getRowIndex(from))
    GridPane.setColumnSpan(to, GridPane.getColumnSpan(from))
    GridPane.setRowSpan(to, GridPane.getRowSpan(from))
    children.remove(from)
    children.add(to)
}

private val PreferencesFx.preferencesFxDialog get() = view.parent.to<PreferencesFxDialog>()
private val PreferencesFx.navigationPresenter get() = field<NavigationPresenter>("navigationPresenter")
private val PreferencesFx.preferencesFxModel get() = field<PreferencesFxModel>("preferencesFxModel")
private val PreferencesFx.buttonBar get() = view.parent.singleChildOf<ButtonBar>()
private val NavigationPresenter.categoryTreeItemMap get() = field<HashMap<Category, FilterableTreeItem<Category>>>("categoryTreeItemMap")
private val ButtonBar.safeButtons get() = buttons.filterIsInstance<Button>()
private val PreferencesFxDialog.preferencesFxView get() = singleChildOf<PreferencesFxView>()
private val PreferencesFxView.navigationView get() = singleChildOf<MasterDetailPane>().detailNode.to<NavigationView>()
private val NavigationView.searchField get() = field<CustomTextField>("searchFld")
private val NavigationView.treeView get() = field<TreeView<Category>>("treeView")
private val PreferencesFxView.breadCrumbView get() = singleChildOf<MasterDetailPane>().masterNode.to<VBox>().singleChildOf<BreadCrumbView>()
private val BreadCrumbView.breadCrumbBar get() = field<BreadCrumbBar<Category>>("breadCrumbBar")
private val PreferencesFxView.categoryController get() = singleChildOf<MasterDetailPane>().masterNode.to<VBox>().singleChildOf<CategoryController>()
private val CategoryController.views get() = field<Map<String, CategoryView>>("views")
private val CategoryView.preferencesFormRenderer get() = field<PreferencesFxFormRenderer>("preferencesFormRenderer")
private val PreferencesFxFormRenderer.groups get() = field<List<PreferencesFxGroupRenderer>>("groups")

private val Parent.deepChildren
    get(): Iterable<Node> = childrenUnmodifiable.toList() + childrenUnmodifiable.filterIsInstance<Parent>().flatMap { it.deepChildren }
        .toList() + childrenUnmodifiable.filterIsInstance<MasterDetailPane>().flatMap { listOf(it.masterNode, it.detailNode) }

private inline fun <reified R> List<*>.singleInstanceOf() = filterIsInstance<R>().single()
private inline fun <reified R> Any.to() = (this as R)
private inline fun <reified R> Parent.singleChildOf() = childrenUnmodifiable.singleInstanceOf<R>()
private inline fun <reified R> Any.field(fieldName: String) = this::class.declaredMemberProperties.single { it.name == fieldName }.apply { javaField!!.isAccessible = true }.getter.call(this)!!.to<R>()


private fun CategoryContext.build() =
    when (visibilityProperty) {
        null -> Category.of(name, icon, *groups.toTypedArray())
        else -> Category.of(name, icon, visibilityProperty, *groups.toTypedArray())
    }.subCategories(*subCategories.toTypedArray())

private fun CategoryContext.applySuperCategoryDescription(superCategoryDescription: String?) {
    if (superCategoryDescription != null) {
        val previousGroups = groups.toList()
        groups.clear()
        group {
            label(superCategoryDescription)
            for (subCategory in this@applySuperCategoryDescription.subCategories) {
                label(subCategory.description.prependIndent("    "))
            }
        }
        groups += previousGroups
    }
}
