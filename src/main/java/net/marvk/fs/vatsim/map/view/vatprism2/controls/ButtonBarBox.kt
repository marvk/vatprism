package net.marvk.fs.vatsim.map.view.vatprism2.controls

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import net.marvk.fs.vatsim.map.view.extensions.getValue
import net.marvk.fs.vatsim.map.view.extensions.setValue

open class ButtonBarBox<Content : Node> : Control() {
    val buttonBarChildren: ObservableList<Node> = FXCollections.observableArrayList()

    val contentProperty = SimpleObjectProperty<Content>(null)
    var content: Content? by contentProperty

    val hideTitleProperty = SimpleBooleanProperty(false)
    var hideTitle: Boolean by hideTitleProperty

    val hideButtonBarProperty = SimpleBooleanProperty(false)
    var hideButtonBar: Boolean by hideButtonBarProperty

    val titleProperty = SimpleStringProperty("")
    var title: String? by titleProperty

    val containerPaddingProperty = SimpleObjectProperty(Insets(1.0))
    var containerPadding: Insets by containerPaddingProperty

    init {
        skin = createDefaultSkin()
    }

    final override fun createDefaultSkin(): Skin<*> = DefaultSkin(this)

    private class DefaultSkin<Content : Node>(private val control: ButtonBarBox<Content>) : SkinBase<ButtonBarBox<Content>>(control) {
        private var contentContainer = VBox().apply {
            styleClass += "main-container"
            paddingProperty().bind(control.containerPaddingProperty)
            VBox.setVgrow(this, Priority.ALWAYS)
        }

        private var rootContainer = VBox(contentContainer).apply {
            maxHeight = Double.MAX_VALUE
            maxWidth = Double.MAX_VALUE
        }

        private var titleLabel = Label().apply {
            padding = Insets(0.0, 0.0, 4.0, 4.0)
            textProperty().bind(control.titleProperty)
        }

        private var buttonBar: HBox? = HBox().apply {
            styleClass += "button-bar-horizontal"
        }

        init {
            children += rootContainer
            Bindings.bindContent(buttonBar!!.children, control.buttonBarChildren)
            control.hideTitleProperty.addListener { _, _, newValue -> setLabel(newValue) }
            control.hideButtonBarProperty.addListener { _, _, newValue -> setButtonBar(newValue) }
            control.contentProperty.addListener { _, _, _ -> setContent() }
            setLabel(control.hideTitle)
            setButtonBar(control.hideButtonBar)
            setContent()
        }

        private fun setLabel(hide: Boolean) {
            rootContainer.children.addIfNotContains(titleLabel, 0, hide)
        }

        private fun setButtonBar(hide: Boolean) {
            contentContainer.children.addIfNotContains(buttonBar, 0, hide)
        }

        private fun setContent() {
            contentContainer.children.addIfNotContains(control.content)
            control.content?.apply {
                VBox.setVgrow(this, Priority.ALWAYS)
            }
        }

        override fun dispose() {
            super.dispose()

            buttonBar = null
            control.content = null
        }
    }
}

private fun ObservableList<Node>.addIfNotContains(node: Node?, index: Int = size, hide: Boolean = false) {
    if (node == null || hide) {
        remove(node)
    } else if (!contains(node)) {
        add(index, node)
    }
}
