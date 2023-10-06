package net.marvk.fs.vatsim.map.view.preferences

data class PainterPreferencesDto(
    val name: String,
    val enabledPreference: PreferenceDto.Boolean?,
    val ungroupedPreferences: List<PreferenceDto>,
    val groupedPreferences: Map<String, List<PreferenceDto>>,
    val subPainterPreferences: List<PainterPreferencesDto>,
)
