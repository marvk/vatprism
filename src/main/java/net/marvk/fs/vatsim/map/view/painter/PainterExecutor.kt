package net.marvk.fs.vatsim.map.view.painter

import javafx.scene.canvas.GraphicsContext
import net.marvk.fs.vatsim.map.view.map.PainterMetric
import java.util.function.Predicate
import java.util.function.Supplier

class PainterExecutor<T> private constructor(
    name: String?,
    val legacyName: String,
    val painter: Painter<T>,
    private val paintablesSupplier: Supplier<Collection<T>>?,
    private val filter: Predicate<T?> = Predicate { true },
) {
    val name: String = name ?: legacyName

    var lastDurationNanos = 0L
        private set
    var lastPainterMetric = PainterMetric()
        private set

    fun paint(c: GraphicsContext) {
        val start = System.nanoTime()
        painter.beforeAllRender()
        if (painter.isEnabled) {
            for (t in paintablesSupplier?.get() ?: listOf(null)) {
                if (filter.test(t)) {
                    paint(c, t)
                }
            }
        }
        painter.afterAllRender()
        lastPainterMetric = painter.createMetricsSnapshot()
        lastDurationNanos = System.nanoTime() - start
    }

    private fun paint(c: GraphicsContext, t: T?) {
        painter.beforeEachRender()
        painter.paint(c, t)
        painter.afterEachRender()
    }

    companion object {
        @JvmStatic
        fun <T> of(name: String?, legacyName: String, painter: Painter<T>): PainterExecutor<T> =
            PainterExecutor(name, legacyName, painter, null)

        @JvmStatic
        @JvmOverloads
        fun <T> ofItem(name: String?, legacyName: String, painter: Painter<T>, paintablesSupplier: Supplier<T>, filter: Predicate<T?> = Predicate { true }): PainterExecutor<T> =
            PainterExecutor(name, legacyName, painter, { getPaintable(paintablesSupplier) }, filter)

        @JvmStatic
        @JvmOverloads
        fun <T> ofCollection(name: String?, legacyName: String, painter: Painter<T>, filter: Predicate<T?> = Predicate { true }, paintablesSupplier: Supplier<Collection<T>>): PainterExecutor<T> =
            PainterExecutor(name, legacyName, painter, paintablesSupplier, filter)

        private fun <T> getPaintable(paintablesSupplier: Supplier<T>): List<T> =
            paintablesSupplier.get()?.let(::listOf) ?: emptyList()
    }
}
