package net.marvk.fs.vatsim.map.view

import javafx.application.Application
import javafx.scene.paint.Color
import javafx.stage.Stage
import net.marvk.fs.vatsim.map.data.Preferences
import net.marvk.fs.vatsim.map.view.preferences.PreferencesView2
import java.nio.file.Path


class TestApplication : Application() {

    override fun start(primaryStage: Stage) {
        val preferences = object : Preferences {
            override fun booleanProperty(key: String?, initialValue: Boolean) = TODO("Not yet implemented")
            override fun stringProperty(key: String?, defaultValue: String?) = TODO("Not yet implemented")
            override fun colorProperty(key: String?, initialValue: Color?) = TODO("Not yet implemented")
            override fun integerProperty(key: String?, defaultValue: Int) = TODO("Not yet implemented")
            override fun doubleProperty(key: String?, initialValue: Double) = TODO("Not yet implemented")
            override fun values() = TODO("Not yet implemented")
        }

        PreferencesView2(preferences, Path.of("")).show()
    }

}

fun main() {
    Application.launch(TestApplication::class.java)
}
