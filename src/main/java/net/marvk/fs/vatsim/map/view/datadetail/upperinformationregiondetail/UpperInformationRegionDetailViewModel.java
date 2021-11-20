package net.marvk.fs.vatsim.map.view.datadetail.upperinformationregiondetail;

import com.google.inject.Inject;
import javafx.scene.paint.Color;
import net.marvk.fs.vatsim.map.data.Preferences;
import net.marvk.fs.vatsim.map.data.UpperInformationRegion;
import net.marvk.fs.vatsim.map.view.datadetail.detailsubview.DataDetailSubViewModel;

public class UpperInformationRegionDetailViewModel extends DataDetailSubViewModel<UpperInformationRegion> {
    private final Preferences preferences;

    @Inject
    public UpperInformationRegionDetailViewModel(final Preferences preferences) {
        this.preferences = preferences;
    }

    public Color getFirColor() {
        return preferences.colorProperty("active_firs.fir.stroke_color").get();
    }

    public Color getLabelColor() {
        return preferences.colorProperty("airports.controller_label_color").get();
    }
}
