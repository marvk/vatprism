package net.marvk.fs.vatsim.map.view.preferences;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.data.*;

public class ColorSchemeViewModel implements ViewModel {
    private final StringProperty nameInput = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper updating = new ReadOnlyBooleanWrapper(false);
    private final ObjectProperty<ColorScheme> colorSchemeToUpdate = new SimpleObjectProperty<>();

    private final ColorSchemeRepository colorSchemeRepository;
    private final Preferences preferences;
    private final ObservableList<ColorScheme> colorSchemes;

    @Inject
    public ColorSchemeViewModel(final ColorSchemeRepository colorSchemeRepository, final Preferences preferences) {
        this.colorSchemeRepository = colorSchemeRepository;
        this.preferences = preferences;
        this.colorSchemes = colorSchemeRepository.list();
        this.nameInput.addListener((observable, oldValue, newValue) -> {
            final ColorScheme maybeColorSchemeToUpdate = colorSchemeRepository
                    .list()
                    .stream()
                    .filter(e -> e.getName().equals(newValue))
                    .findFirst()
                    .orElse(null);

            colorSchemeToUpdate.set(maybeColorSchemeToUpdate);
        });
        this.updating.bind(colorSchemeToUpdate.isNotNull());
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
    public void saveCurrent() {
        final String name = getNameInput();

        if (!name.isBlank()) {
            createOrUpdateColorScheme(name);
        }
    }

    private void createOrUpdateColorScheme(final String name) throws RepositoryException {
        final ConfigFilePreferences preferences = (ConfigFilePreferences) this.preferences;
        final ColorScheme previousColorScheme = colorSchemeToUpdate.get();

        if (previousColorScheme == null) {
            colorSchemeRepository.create(preferences.exportColorScheme(name));
        } else {
            colorSchemeRepository.update(preferences.exportColorScheme(previousColorScheme));
        }
    }

    @SneakyThrows
    public void delete(final ColorScheme colorScheme) {
        colorSchemeRepository.delete(colorScheme);
    }

    public String getNameInput() {
        return nameInput.get();
    }

    public StringProperty nameInputProperty() {
        return nameInput;
    }

    public void setNameInput(final String nameInput) {
        this.nameInput.set(nameInput);
    }

    public boolean isUpdating() {
        return updating.get();
    }

    public ReadOnlyBooleanProperty updatingProperty() {
        return updating.getReadOnlyProperty();
    }
}
