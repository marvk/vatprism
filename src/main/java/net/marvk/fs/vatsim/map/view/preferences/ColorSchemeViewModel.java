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
    private final SortedList<ColorScheme> packagedColorSchemes;
    private final SortedList<ColorScheme> sortedColorSchemes;

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

        this.packagedColorSchemes = new SortedList<>(packagedColorSchemeRepository.list(),
                Comparator
                        .comparing(ColorScheme::getName, (o1, o2) -> Boolean.compare(o1.startsWith("VATprism"), o2.startsWith("VATprism")))
                        .reversed()
                        .thenComparing(ColorScheme::getName, String::compareTo)
        );
        this.sortedColorSchemes = new SortedList<>(this.colorSchemes,
                Comparator.comparing(ColorScheme::getName)
        );
    }

    public ObservableList<ColorScheme> packagedColorSchemes() {
        return FXCollections.unmodifiableObservableList(packagedColorSchemes);
    }

    public ObservableList<ColorScheme> customColorSchemes() {
        return FXCollections.unmodifiableObservableList(sortedColorSchemes);
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
