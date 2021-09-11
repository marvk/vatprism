package net.marvk.fs.vatsim.map.view.preferences;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import lombok.SneakyThrows;
import net.marvk.fs.vatsim.map.data.*;
import net.marvk.fs.vatsim.map.view.Notifications;

import java.util.Comparator;

public class ColorSchemeViewModel implements ViewModel {
    private final StringProperty nameInput = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper updating = new ReadOnlyBooleanWrapper(false);
    private final ObjectProperty<ColorScheme> colorSchemeToUpdate = new SimpleObjectProperty<>();

    private final PackagedColorSchemeRepository packagedColorSchemeRepository;
    private final CustomColorSchemeRepository customColorSchemeRepository;
    private final Preferences preferences;
    private final ObservableList<ColorScheme> colorSchemes;

    @Inject
    public ColorSchemeViewModel(
            final PackagedColorSchemeRepository packagedColorSchemeRepository,
            final CustomColorSchemeRepository customColorSchemeRepository,
            final Preferences preferences
    ) {
        this.packagedColorSchemeRepository = packagedColorSchemeRepository;
        this.customColorSchemeRepository = customColorSchemeRepository;
        this.preferences = preferences;
        this.colorSchemes = customColorSchemeRepository.list();
        this.nameInput.addListener((observable, oldValue, newValue) -> {
            final ColorScheme maybeColorSchemeToUpdate = customColorSchemeRepository
                    .list()
                    .stream()
                    .filter(e -> e.getName().equals(newValue))
                    .findFirst()
                    .orElse(null);

            colorSchemeToUpdate.set(maybeColorSchemeToUpdate);
        });
        this.updating.bind(colorSchemeToUpdate.isNotNull());

        Notifications.SET_THEME.subscribe(s ->
                packagedColorSchemeRepository.findByName(s).ifPresent(ColorSchemeViewModel.this::set)
        );
    }

    public ObservableList<ColorScheme> packagedColorSchemes() {
        return packagedColorSchemeRepository.list();
    }

    public ObservableList<ColorScheme> customColorSchemes() {
        final SortedList<ColorScheme> sortedSchemes = new SortedList<>(this.colorSchemes);
        sortedSchemes.setComparator(Comparator.comparing(ColorScheme::getName));

        return FXCollections.unmodifiableObservableList(sortedSchemes);
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
            customColorSchemeRepository.create(preferences.exportColorScheme(name));
        } else {
            customColorSchemeRepository.update(preferences.exportColorScheme(previousColorScheme));
        }
    }

    @SneakyThrows
    public void delete(final ColorScheme colorScheme) {
        customColorSchemeRepository.delete(colorScheme);
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
