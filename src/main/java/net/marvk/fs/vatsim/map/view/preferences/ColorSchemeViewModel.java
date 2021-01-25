package net.marvk.fs.vatsim.map.view.preferences;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.data.ColorScheme;
import net.marvk.fs.vatsim.map.data.ColorSchemeRepository;
import net.marvk.fs.vatsim.map.data.ConfigFilePreferences;
import net.marvk.fs.vatsim.map.data.Preferences;

public class ColorSchemeViewModel implements ViewModel {
    private final ColorSchemeRepository colorSchemeRepository;
    private final Preferences preferences;
    private final ObservableList<ColorScheme> colorSchemes;

    @Inject
    public ColorSchemeViewModel(final ColorSchemeRepository colorSchemeRepository, final Preferences preferences) {
        this.colorSchemeRepository = colorSchemeRepository;
        this.preferences = preferences;
        this.colorSchemes = colorSchemeRepository.list();

    }

    public ObservableList<ColorScheme> colorSchemes() {
        return FXCollections.unmodifiableObservableList(colorSchemes);
    }

    public void set(final ColorScheme colorScheme) {
        if (colorScheme != null) {
            ((ConfigFilePreferences) preferences).importColorScheme(colorScheme);
        }
    }

    @SneakyThrows
    public void saveCurrent(final String name) {
        final ColorScheme colorScheme = ((ConfigFilePreferences) preferences).exportColorScheme(name);
        colorSchemeRepository.create(colorScheme);
    }

    @SneakyThrows
    public void delete(final ColorScheme colorScheme) {
        colorSchemeRepository.delete(colorScheme);
    }
}
