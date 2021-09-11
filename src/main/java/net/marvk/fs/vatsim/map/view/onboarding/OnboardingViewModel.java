package net.marvk.fs.vatsim.map.view.onboarding;

import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import net.marvk.fs.vatsim.map.view.Notifications;

public class OnboardingViewModel implements ViewModel {
    private final ReadOnlyBooleanWrapper onboardingComplete = new ReadOnlyBooleanWrapper();

    public void setTheme(final Theme theme) {
        Notifications.SET_THEME.publish("VATprism " + theme.getThemeName());
        skip();
    }

    public void skip() {
        onboardingComplete.set(true);
    }

    public boolean isOnboardingComplete() {
        return onboardingComplete.get();
    }

    public ReadOnlyBooleanProperty onboardingCompleteProperty() {
        return onboardingComplete.getReadOnlyProperty();
    }

    enum Theme {
        LIGHT("Light"), DARK("Dark"), CLASSIC_SEPIA("Classic Sepia"), EARTH("Earth");

        private final String themeName;

        Theme(final String name) {
            this.themeName = name;
        }

        public String getThemeName() {
            return themeName;
        }
    }
}
