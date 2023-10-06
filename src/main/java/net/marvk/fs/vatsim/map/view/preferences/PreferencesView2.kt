package net.marvk.fs.vatsim.map.view.preferences

import com.google.inject.Inject
import com.google.inject.Singleton
import com.google.inject.name.Named
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.FXCollections
import net.marvk.fs.vatsim.map.data.Preferences
import net.marvk.fs.vatsim.map.extensions.createLogger
import org.scenicview.ScenicView
import java.awt.Desktop
import java.io.IOException
import java.nio.file.Path

@Singleton
class PreferencesView2 @Inject constructor(
    private val preferences: Preferences,
    private val painterPreferencesLoader: PainterPreferencesLoader,
    @Named("userConfigDir") private val configDirectory: Path,
) {
    private val log = createLogger()

    private val preferencesFx by lazy {
        preferencesFx(javaClass) {
            category("Appearance", "Personalize the look and feel of VATprism") {
                category("UI") {
                    group {
                        checkbox(SimpleBooleanProperty()) {
                            textAfter = "Remember last window position"
                        }
                        combobox(FXCollections.observableArrayList(8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72), SimpleObjectProperty()) {
                            textBefore = "UI Font size:"
                        }
                        checkbox(SimpleBooleanProperty()) {
                            textAfter = "Twitch integration"
                        }
                    }
                }

                category("Map", "Personalize the look of the Map, including color scheme and draw settings") {
                    category("General") {
                        group {
                            combobox(FXCollections.observableArrayList(8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72), SimpleObjectProperty()) {
                                textBefore = "Map Font size:"
                            }
                            doubleSpinner(1.1, 16.0, 0.1, SimpleDoubleProperty()) {
                                textBefore = "Zoom speed:"
                            }
                        }
                    }

                    category("Context Menu") {
                        group {
                            checkbox(SimpleBooleanProperty()) {
                                textAfter = "Show hidden FIRs"
                            }
                            checkbox(SimpleBooleanProperty()) {
                                textAfter = "Show hidden UIRs"
                            }
                            checkbox(SimpleBooleanProperty()) {
                                textAfter = "Show hidden airports"
                            }
                            checkbox(SimpleBooleanProperty()) {
                                textAfter = "Show hidden pilots"
                            }
                        }
                    }

                    category("Layers", "Customize the look of the map") {
                        painters()
                    }
                }
            }


            category("Other") {
                group {
                    button("Open Config Directory", { openConfigDirectory() })
                }
            }

            category("Advanced") {
                val enableAdvancedMode = SimpleBooleanProperty(true)
                val advancedModeDisabled = enableAdvancedMode.not()
                group {
                    checkbox(enableAdvancedMode) {
                        textAfter = "Enable advanced mode"
                        textExplanation = "No guarantees are made. Only use this if you know what you are doing."
                    }
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Prerelease updates"
                        textExplanation = "Prerelease updates are not stable, anything might break at any time"
                        disableProperty = advancedModeDisabled
                    }
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Cache static map data"
                        textExplanation = "Accelerates startup time, outdated map data will still be updated automatically"
                        disableProperty = advancedModeDisabled
                    }
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Local data file discovery"
                        textExplanation = "VATprism will look for data files placed in the config directory, intended to test changes locally"
                        disableProperty = advancedModeDisabled
                    }
                }

                group("Debug") {
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Debug mode"
                        textExplanation = "Show additional data that might be useful for debugging"
                        disableProperty = advancedModeDisabled
                    }
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Prune old logs"
                        textExplanation = "Delete logs older than 14 days automatically at startup"
                        disableProperty = advancedModeDisabled
                    }
                    combobox(FXCollections.observableArrayList("TRACE", "DEBUG", "INFO"), SimpleObjectProperty()) {
                        textBefore = "Log level"
                        disableProperty = advancedModeDisabled
                    }
                    button("Scenic View", { ScenicView.show(this@preferencesFx.preferencesFx.view) }) {
                        disableProperty = advancedModeDisabled
                    }
                }

                group("Metrics") {
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Enable"
                        disableProperty = advancedModeDisabled
                    }
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Averages"
                        disableProperty = advancedModeDisabled
                    }
                    checkbox(SimpleBooleanProperty()) {
                        textAfter = "Chart"
                        disableProperty = advancedModeDisabled
                    }
                    intSpinner(50, 500, 10, SimpleIntegerProperty()) {
                        textAfter = "Height"
                        disableProperty = advancedModeDisabled
                    }
                }
            }
        }
    }

    fun show() {
        preferencesFx.show(false)
    }


    private fun CategoryContext.painters() {
        val painterPreferences = painterPreferencesLoader.loadSettings()

        for (painterPreference in painterPreferences) {
            painterCategory(painterPreference)
        }
    }

    private fun CategoryContext.painterCategory(painterPreferencesDto: PainterPreferencesDto) {
        category(painterPreferencesDto.name) {
            if (painterPreferencesDto.enabledPreference != null) {
                group {
                    painterPreference(painterPreferencesDto.enabledPreference)
                }
            }

            if (painterPreferencesDto.ungroupedPreferences.isNotEmpty()) {
                painterPreferencesGroup(painterPreferencesDto.ungroupedPreferences, "", painterPreferencesDto.enabledPreference?.property?.not())
            }

            for ((groupName, group) in painterPreferencesDto.groupedPreferences) {
                painterPreferencesGroup(group, groupName, painterPreferencesDto.enabledPreference?.property?.not())
            }

            for (subPainterPreference in painterPreferencesDto.subPainterPreferences) {
                painterCategory(subPainterPreference)
            }
        }
    }

    private fun CategoryContext.painterPreferencesGroup(preferenceDtos: List<PreferenceDto>, groupName: String, enabled: ObservableBooleanValue? = null) {
        group(groupName) {
            for (preference in preferenceDtos) {
                painterPreference(preference, enabled)
            }
        }
    }

    private fun GroupContext.painterPreference(preference: PreferenceDto, disableProperty: ObservableBooleanValue? = null) {
        val parameter = preference.parameter
        val hintText = parameter.hintText.takeIf(String::isNotBlank)

        when (preference) {
            is PreferenceDto.Int -> {
                intSpinner(parameter.min.toInt(), parameter.max.toInt(), parameter.stepSize.toInt(), preference.property) {
                    this.textBefore = parameter.name
                    this.disableProperty = disableProperty
                    this.textExplanation = hintText
                }
            }

            is PreferenceDto.Double ->
                doubleSpinner(parameter.min, parameter.max, parameter.stepSize, preference.property) {
                    this.textBefore = parameter.name
                    this.disableProperty = disableProperty
                    this.textExplanation = hintText
                }

            is PreferenceDto.Boolean ->
                checkbox(preference.property) {
                    this.textAfter = parameter.name
                    this.disableProperty = disableProperty
                    this.textExplanation = hintText
                }

            is PreferenceDto.Color ->
                colorPicker(preference.property) {
                    this.textBefore = parameter.name
                    this.disableProperty = disableProperty
                    this.textExplanation = hintText
                }
        }
    }

    private fun openConfigDirectory() {
        try {
            Desktop.getDesktop().browseFileDirectory(configDirectory.toFile())
        } catch (e1: UnsupportedOperationException) {
            log.warn("Failed to open config directory via Desktop API, trying explorer...")
            try {
                val pathString: String = configDirectory.toAbsolutePath().toString().replace("/".toRegex(), "\\\\")
                val proc = Runtime.getRuntime().exec("explorer.exe $pathString")
                proc.waitFor()
            } catch (e: IOException) {
                log.error("Failed to open config directory", e)
            } catch (e: InterruptedException) {
                log.error("Failed to open config directory", e)
            }
        }
    }
}
