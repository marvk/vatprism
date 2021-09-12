package net.marvk.fs.vatsim.map.view.onboarding;

import com.google.inject.Inject;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import lombok.extern.log4j.Log4j2;
import net.marvk.fs.vatsim.map.api.VatprismApi;
import net.marvk.fs.vatsim.map.api.VatprismApiException;
import net.marvk.fs.vatsim.map.view.Notifications;

@Log4j2
public class OnboardingViewModel implements ViewModel {
    private final ReadOnlyBooleanWrapper onboardingComplete = new ReadOnlyBooleanWrapper();
    private final VatprismApi vatprismApi;

    @Inject
    public OnboardingViewModel(final VatprismApi vatprismApi) {
        this.vatprismApi = vatprismApi;
    }

    public void setTheme(final Theme theme) {
        Notifications.SET_THEME.publish("VATprism " + theme.getThemeName());
        trySubmitThemeChoiceApiRequest(theme);
        onboardingComplete.set(true);
    }

    private void trySubmitThemeChoiceApiRequest(final Theme theme) {
        try {
            vatprismApi.submitThemeChoice(theme.getThemeName());
        } catch (final VatprismApiException e) {
            log.error("Failed to send request to theme choice endpoint", e);
        }
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
