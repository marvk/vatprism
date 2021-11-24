package net.marvk.fs.vatsim.map.view.motds;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import net.marvk.fs.vatsim.map.commons.motd.MessageOfTheDay;
import net.marvk.fs.vatsim.map.data.MotdsRepository;
import net.marvk.fs.vatsim.map.data.Preferences;

public class MotdsViewModel implements ViewModel {
    private final MotdsRepository motdsRepository;
    private final Preferences preferences;

    private final ReadOnlyObjectWrapper<MessageOfTheDay> selectedMessageOfTheDay = new ReadOnlyObjectWrapper<>();
    private final IntegerProperty selectedIndex = new SimpleIntegerProperty();

    private final ReadOnlyIntegerWrapper size = new ReadOnlyIntegerWrapper();

    @Inject
    public MotdsViewModel(
            final MotdsRepository motdsRepository,
            final Preferences preferences
    ) {
        this.motdsRepository = motdsRepository;
        motdsRepository.reloadAsync(null);
        this.preferences = preferences;
        this.size.bind(Bindings.createIntegerBinding(() -> messagesOfTheDay().size(), messagesOfTheDay()));
        this.selectedMessageOfTheDay.bind(Bindings.createObjectBinding(() -> {
            final Integer index = selectedIndex.getValue();
            if (index == null || index < 0 || index >= size.get()) {
                return null;
            }

            return messagesOfTheDay().get(index);
        }, selectedIndex));
    }

    public ObservableList<MessageOfTheDay> messagesOfTheDay() {
        return motdsRepository.list();
    }

    public int getSize() {
        return size.get();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size.getReadOnlyProperty();
    }

    public MessageOfTheDay getSelectedMessageOfTheDay() {
        return selectedMessageOfTheDay.get();
    }

    public ReadOnlyObjectProperty<MessageOfTheDay> selectedMessageOfTheDayProperty() {
        return selectedMessageOfTheDay.getReadOnlyProperty();
    }

    public int getSelectedIndex() {
        return selectedIndex.get();
    }

    public IntegerProperty selectedIndexProperty() {
        return selectedIndex;
    }

    public void setSelectedIndex(final Integer selectedIndex) {
        this.selectedIndex.set(selectedIndex);
    }

    public void setSelectedMessageOfTheDay(final MessageOfTheDay selectedMessageOfTheDay) {
        this.selectedMessageOfTheDay.set(selectedMessageOfTheDay);
    }
}
