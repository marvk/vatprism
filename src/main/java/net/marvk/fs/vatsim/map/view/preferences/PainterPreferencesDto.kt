package net.marvk.fs.vatsim.map.view.preferences

data class PainterPreferencesDto(
    val name: String,
    val enabledPreference: PreferenceDto?,
    val preferences: List<PreferenceDto>,
    val subPainterPreferences: List<PainterPreferencesDto>,
)
