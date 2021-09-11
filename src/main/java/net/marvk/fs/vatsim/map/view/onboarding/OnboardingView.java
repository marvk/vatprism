package net.marvk.fs.vatsim.map.view.onboarding;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;

public class OnboardingView implements FxmlView<OnboardingViewModel> {
    @InjectViewModel
    private OnboardingViewModel viewModel;

    @FXML
    private void skip() {
        viewModel.skip();
    }

    @FXML
    private void setThemeToLight() {
        viewModel.setTheme(OnboardingViewModel.Theme.LIGHT);
    }

    @FXML
    private void setThemeToEarth() {
        viewModel.setTheme(OnboardingViewModel.Theme.EARTH);
    }

    @FXML
    private void setThemeToDark() {
        viewModel.setTheme(OnboardingViewModel.Theme.DARK);
    }

    @FXML
    private void setThemeToClassicSepia() {
        viewModel.setTheme(OnboardingViewModel.Theme.CLASSIC_SEPIA);
    }
}
